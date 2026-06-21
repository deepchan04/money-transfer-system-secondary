import {
  Component,
  OnInit,
  AfterViewInit,
  ViewChild,
  ElementRef,
  Inject,
  OnDestroy
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { PostService } from '../../service/post.service';
import { ScratchCard } from '../rewards.component';

export interface ScratchDialogData {
  card: ScratchCard;
}

@Component({
  selector: 'app-scratch-dialog',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, MatDialogModule],
  templateUrl: './scratch-dialog.component.html',
  styleUrls: ['./scratch-dialog.component.scss']
})
export class ScratchDialogComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('scratchCanvas') canvasRef!: ElementRef<HTMLCanvasElement>;

  card: ScratchCard;
  revealedCard: ScratchCard | null = null;
  isRevealing = false;
  isRevealed = false;
  isApiCalling = false;
  copySuccess = false;
  confettiParticles: Array<{
    x: number; y: number; vx: number; vy: number;
    color: string; size: number; alpha: number;
  }> = [];

  private ctx!: CanvasRenderingContext2D;
  private isDrawing = false;
  private checkInterval?: ReturnType<typeof setInterval>;

  readonly SCRATCH_THRESHOLD = 0.4; // 40% scratched triggers auto-reveal
  readonly CANVAS_W = 320;
  readonly CANVAS_H = 180;

  constructor(
    public dialogRef: MatDialogRef<ScratchDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ScratchDialogData,
    private postService: PostService
  ) {
    this.card = data.card;
  }

  ngOnInit(): void {}

  ngAfterViewInit(): void {
    this.setupCanvas();
  }

  ngOnDestroy(): void {
    if (this.checkInterval) clearInterval(this.checkInterval);
  }

  setupCanvas(): void {
    const canvas = this.canvasRef.nativeElement;
    canvas.width = this.CANVAS_W;
    canvas.height = this.CANVAS_H;
    this.ctx = canvas.getContext('2d')!;

    // Draw emerald scratchcard overlay
    const grad = this.ctx.createLinearGradient(0, 0, this.CANVAS_W, this.CANVAS_H);
    grad.addColorStop(0, '#064e3b');
    grad.addColorStop(0.3, '#065f46');
    grad.addColorStop(0.55, '#22C55E');
    grad.addColorStop(0.75, '#16A34A');
    grad.addColorStop(1, '#064e3b');
    this.ctx.fillStyle = grad;
    this.ctx.fillRect(0, 0, this.CANVAS_W, this.CANVAS_H);

    // Noise texture overlay
    for (let i = 0; i < 5000; i++) {
      const x = Math.random() * this.CANVAS_W;
      const y = Math.random() * this.CANVAS_H;
      const alpha = Math.random() * 0.06;
      this.ctx.fillStyle = `rgba(0,0,0,${alpha})`;
      this.ctx.fillRect(x, y, 1, 1);
    }

    // "SCRATCH HERE" text
    this.ctx.fillStyle = 'rgba(255,255,255,0.55)';
    this.ctx.font = 'bold 13px "IBM Plex Sans", sans-serif';
    this.ctx.textAlign = 'center';
    this.ctx.letterSpacing = '0.15em';
    this.ctx.fillText('SCRATCH HERE', this.CANVAS_W / 2, this.CANVAS_H / 2 - 10);
    this.ctx.font = '11px "IBM Plex Sans", sans-serif';
    this.ctx.fillStyle = 'rgba(255,255,255,0.38)';
    this.ctx.fillText('Reveal your reward', this.CANVAS_W / 2, this.CANVAS_H / 2 + 12);

    // Mouse events
    canvas.addEventListener('mousedown', (e) => this.startScratch(e));
    canvas.addEventListener('mousemove', (e) => this.scratch(e));
    canvas.addEventListener('mouseup', () => { this.isDrawing = false; });
    canvas.addEventListener('mouseleave', () => { this.isDrawing = false; });

    // Touch events
    canvas.addEventListener('touchstart', (e) => { e.preventDefault(); this.startScratch(e.touches[0]); }, { passive: false });
    canvas.addEventListener('touchmove', (e) => { e.preventDefault(); this.scratch(e.touches[0]); }, { passive: false });
    canvas.addEventListener('touchend', () => { this.isDrawing = false; });

    // Periodically check how much has been scratched
    this.checkInterval = setInterval(() => this.checkScratchProgress(), 300);
  }

  private getPos(e: MouseEvent | Touch): { x: number; y: number } {
    const rect = this.canvasRef.nativeElement.getBoundingClientRect();
    const scaleX = this.CANVAS_W / rect.width;
    const scaleY = this.CANVAS_H / rect.height;
    return {
      x: (e.clientX - rect.left) * scaleX,
      y: (e.clientY - rect.top) * scaleY
    };
  }

  startScratch(e: MouseEvent | Touch): void {
    this.isDrawing = true;
    const { x, y } = this.getPos(e);
    this.ctx.globalCompositeOperation = 'destination-out';
    this.ctx.beginPath();
    this.ctx.arc(x, y, 22, 0, Math.PI * 2);
    this.ctx.fill();
  }

  scratch(e: MouseEvent | Touch): void {
    if (!this.isDrawing || this.isRevealing || this.isRevealed) return;
    const { x, y } = this.getPos(e);
    this.ctx.globalCompositeOperation = 'destination-out';
    this.ctx.beginPath();
    this.ctx.arc(x, y, 22, 0, Math.PI * 2);
    this.ctx.fill();
  }

  private checkScratchProgress(): void {
    if (this.isRevealing || this.isRevealed) return;
    const imageData = this.ctx.getImageData(0, 0, this.CANVAS_W, this.CANVAS_H);
    let transparent = 0;
    for (let i = 3; i < imageData.data.length; i += 4) {
      if (imageData.data[i] < 128) transparent++;
    }
    const total = this.CANVAS_W * this.CANVAS_H;
    const ratio = transparent / total;
    if (ratio >= this.SCRATCH_THRESHOLD) {
      this.triggerReveal();
    }
  }

  private triggerReveal(): void {
    if (this.isRevealing || this.isRevealed) return;
    this.isRevealing = true;
    if (this.checkInterval) clearInterval(this.checkInterval);

    // Wipe remaining foil
    this.ctx.globalCompositeOperation = 'destination-out';
    this.ctx.fillRect(0, 0, this.CANVAS_W, this.CANVAS_H);

    // Call backend to reveal the actual card details
    this.isApiCalling = true;
    this.postService.scratchCard(this.card.id).subscribe({
      next: (revealed) => {
        this.revealedCard = revealed;
        this.isRevealed = true;
        this.isRevealing = false;
        this.isApiCalling = false;
        this.launchConfetti();
      },
      error: () => {
        this.isRevealing = false;
        this.isApiCalling = false;
        this.isRevealed = true;
      }
    });
  }

  private launchConfetti(): void {
    const colors = ['#22C55E', '#16A34A', '#34d399', '#60a5fa', '#a78bfa', '#f87171', '#fb923c'];
    this.confettiParticles = Array.from({ length: 60 }, () => ({
      x: Math.random() * 340,
      y: Math.random() * -80,
      vx: (Math.random() - 0.5) * 3,
      vy: Math.random() * 3 + 1,
      color: colors[Math.floor(Math.random() * colors.length)],
      size: Math.random() * 7 + 3,
      alpha: 1
    }));
    this.animateConfetti();
  }

  private animateConfetti(): void {
    const step = () => {
      this.confettiParticles = this.confettiParticles
        .map(p => ({ ...p, x: p.x + p.vx, y: p.y + p.vy, alpha: p.alpha - 0.012 }))
        .filter(p => p.alpha > 0);
      if (this.confettiParticles.length > 0) {
        requestAnimationFrame(step);
      }
    };
    requestAnimationFrame(step);
  }

  copyCode(): void {
    const code = this.revealedCard?.couponCode;
    if (!code) return;
    navigator.clipboard.writeText(code).then(() => {
      this.copySuccess = true;
      setTimeout(() => { this.copySuccess = false; }, 2000);
    }).catch(() => {});
  }

  close(): void {
    const result = this.revealedCard ?? (this.isRevealed ? this.card : null);
    this.dialogRef.close(result);
  }
}
