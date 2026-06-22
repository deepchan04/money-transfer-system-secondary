import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { RouterModule } from '@angular/router';
import { PostService } from '../services/post.service';
import { ScratchDialogComponent } from './scratch-dialog/scratch-dialog.component';
import { RewardService } from '../services/reward.service';

export interface ScratchCard {
  id: number;
  scratched: boolean;
  title: string;
  description: string;
  couponCode: string;
  createdAt: string;
  scratchedAt: string | null;
}

export interface RewardStatus {
  rewardPoints: number;
  totalPointsEarned: number;
  unscratchedCount: number;
  scratchedCount: number;
}

@Component({
  selector: 'app-rewards',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatDialogModule,
    RouterModule
  ],
  templateUrl: './rewards.component.html',
  styleUrls: ['./rewards.component.scss']
})
export class RewardsComponent implements OnInit {
  rewardStatus: RewardStatus | null = null;
  scratchCards: ScratchCard[] = [];
  isLoading = true;
  errorMessage = '';

  constructor(
    private postService: PostService,
    private dialog: MatDialog,
    private rewardService: RewardService
  ) {}

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.isLoading = true;
    this.postService.getRewardStatus().subscribe({
      next: (status) => {
        this.rewardStatus = status;
        this.loadScratchCards();
      },
      error: (err) => {
        console.error('Error loading reward status:', err);
        this.errorMessage = 'Failed to load rewards. Please try again.';
        this.isLoading = false;
      }
    });
  }

  loadScratchCards(): void {
    this.postService.getScratchCards().subscribe({
      next: (cards) => {
        this.scratchCards = cards;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading scratchcards:', err);
        this.isLoading = false;
      }
    });
  }

  get unscratchedCards(): ScratchCard[] {
    return this.scratchCards.filter(c => !c.scratched);
  }

  get scratchedCards(): ScratchCard[] {
    return this.scratchCards.filter(c => c.scratched);
  }

  /** Progress toward next scratchcard unlock (within the current 10-point block) */
  get progressToNextCard(): number {
    if (!this.rewardStatus) return 0;
    return (this.rewardStatus.totalPointsEarned % 10) * 10; // 0-100%
  }

  get pointsToNextCard(): number {
    if (!this.rewardStatus) return 10;
    return 10 - (this.rewardStatus.totalPointsEarned % 10);
  }

  openScratchDialog(card: ScratchCard): void {
    const dialogRef = this.dialog.open(ScratchDialogComponent, {
      data: { card },
      panelClass: 'scratch-dialog-panel',
      disableClose: true,
      width: '380px',
      maxWidth: '95vw'
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result?.scratched) {
        // Update the card in-place after scratching
        const idx = this.scratchCards.findIndex(c => c.id === result.id);
        if (idx !== -1) {
          this.scratchCards[idx] = result;
          this.scratchCards = [...this.scratchCards];
        }
        // Update counts in status
        if (this.rewardStatus) {
          this.rewardStatus = {
            ...this.rewardStatus,
            unscratchedCount: Math.max(0, this.rewardStatus.unscratchedCount - 1),
            scratchedCount: this.rewardStatus.scratchedCount + 1
          };
        }

        this.rewardService.refreshRewardCount();
      }
    });
  }

  getBrandFromCode(couponCode: string): string {
    return couponCode?.split('-')[0] || '';
  }

  getBrandColor(couponCode: string): string {
    // Force emerald branding for all revealed cards per design decision
    return '#22C55E';
  }

  getBrandIcon(couponCode: string): string {
    const brand = this.getBrandFromCode(couponCode);
    const icons: Record<string, string> = {
      ZOMATO: 'restaurant',
      SWIGGY: 'delivery_dining',
      UBER: 'directions_car',
      MYNTRA: 'checkroom',
      BMS: 'movie',
      SPOTIFY: 'headphones',
      AJIO: 'storefront',
      PHARMEASY: 'local_pharmacy'
    };
    return icons[brand] || 'card_giftcard';
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
  }

  copyCode(code: string): void {
    navigator.clipboard.writeText(code).catch(() => {
      // Fallback
      const el = document.createElement('textarea');
      el.value = code;
      document.body.appendChild(el);
      el.select();
      document.execCommand('copy');
      document.body.removeChild(el);
    });
  }
}
