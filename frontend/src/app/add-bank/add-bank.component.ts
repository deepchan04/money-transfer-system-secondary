import { Component, OnInit, OnDestroy } from '@angular/core';
import { AuthService } from '../auth/auth.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
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
    MatButtonModule,
    MatIconModule
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
  showAccountPassword = false;
  countdown = 5;
  progressWidth = 0;
  private countdownSubscription?: Subscription;
  private accPattern: RegExp = /^ACC\d{1,6}$/i;

  constructor(
    private postService: PostService,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit() {
    // Auto-populate VPA ID from AuthService
    const user = this.authService.getCurrentUser();
    if (user) {
      this.vpaId = user.vpa?.vpaId || user.vpaId || '';
    }
  }

  get accountNumberInvalid(): boolean {
    return !!(this.accountNumber && !this.accPattern.test(this.accountNumber));
  }

  get accountPasswordInvalid(): boolean {
    return !!(this.accountPassword && this.accountPassword.length < 6);
  }

  linkBank() {
    // Clear previous errors
    this.errorMessage = '';
    // Basic client-side validation (format checks)
    if (!this.accountNumber || !this.accountPassword) {
      this.errorMessage = 'Please enter account number and password';
      return;
    }

    // Account number must begin with 'ACC' and be followed by 1-6 digits
    if (!this.accPattern.test(this.accountNumber)) {
      this.errorMessage = "Account number must begin with 'ACC' and be followed by 1 to 6 digits";
      return;
    }

    // Password minimum length (same message as login)
    if (this.accountPassword.length < 6) {
      this.errorMessage = 'Password must be at least 6 characters long';
      return;
    }

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
          this.authService.setCurrentUser(response);
          this.isLoading = false;
          this.startCountdown();
        },
        error: (err) => {
          console.error('Error linking bank account:', err);
          // For conflict (409) prefer the backend message directly (e.g. AccountLinkedException)
          if(err.status === 0) {
          this.errorMessage = 'Server is currently down. Please try later.';
        }
          else if (err.status === 409) {
            this.errorMessage = (typeof err.error === 'string') ? err.error : (err.error?.message || 'Bank account is already linked.');
          } else {
            this.errorMessage = err.error?.message ? err.error.message : (typeof err.error === 'string' ? err.error : 'Error linking bank account. Please try again.');
          }
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
