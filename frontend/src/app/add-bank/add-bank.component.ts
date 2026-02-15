import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { NavbarComponent } from '../navbar/navbar.component';
import { Router, RouterModule } from '@angular/router';
import { PostService } from '../service/post.service';
import { interval, Subscription } from 'rxjs';
import { take } from 'rxjs/operators';

@Component({
  selector: 'app-add-bank',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    NavbarComponent,
    MatCardModule,
    MatInputModule,
    MatButtonModule
  ],
  templateUrl: './add-bank.component.html',
  styleUrls: ['./add-bank.component.scss']
})
export class AddBankComponent implements OnInit, OnDestroy {
  vpaId = '';
  accountNumber = '';
  accountPassword = '';
  success = false;
  responseMessage = '';
  errorMessage = '';
  isLoading = false;
  countdown = 5;
  progressWidth = 0;
  private countdownSubscription?: Subscription;

  constructor(
    private postService: PostService,
    private router: Router
  ) {}

  ngOnInit() {
    // Auto-populate VPA ID from localStorage
    const userData = localStorage.getItem('user');
    if (userData) {
      const user = JSON.parse(userData);
      this.vpaId = user.vpa?.vpaId || user.vpaId || '';
    }
  }

  linkBank() {
    // Clear previous errors
    this.errorMessage = '';
    this.success = true;
    this.isLoading = true;

    const linkData = {
      vpaId: this.vpaId,
      accountNumber: this.accountNumber,
      accountPassword: this.accountPassword
    };

    // Show loading for 1 second before making API call
    setTimeout(() => {
      this.postService.linkBankAccount(linkData).subscribe({
        next: (response) => {
          console.log('Bank account linked successfully!', response);
          this.responseMessage = JSON.stringify(response);
          localStorage.setItem('user', JSON.stringify(response));
          this.isLoading = false;
          this.startCountdown();
        },
        error: (err) => {
          console.error('Error linking bank account:', err);
          this.errorMessage = err.error || 'Error linking bank account. Please try again.';
          this.success = false;
          this.isLoading = false;
        }
      });
    }, 1000);
  }

  private startCountdown(): void {
    this.countdown = 5;
    this.progressWidth = 100;

    // Update countdown every second
    this.countdownSubscription = interval(1000)
      .pipe(take(5))
      .subscribe({
        next: (count) => {
          this.countdown = 5 - (count + 1);
          this.progressWidth = ((5 - (count + 1)) / 5) * 100;
        },
        complete: () => {
          this.router.navigate(['/dashboard']);
        }
      });
  }

  ngOnDestroy(): void {
    // Clean up subscription
    if (this.countdownSubscription) {
      this.countdownSubscription.unsubscribe();
    }
  }
}