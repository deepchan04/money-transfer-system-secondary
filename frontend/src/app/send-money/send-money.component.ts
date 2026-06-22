import { Component, OnInit, OnDestroy } from '@angular/core';
import { AuthService } from '../auth/auth.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { trigger, transition, style, animate } from '@angular/animations';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { Router, RouterModule } from '@angular/router';
import { PostService } from '../service/post.service';
import { interval, Subscription } from 'rxjs';
import { take } from 'rxjs/operators';
import { RewardService } from '../service/reward.service';

@Component({
  selector: 'app-send-money',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    RouterModule
  ],
  animations: [
    trigger('fadeIn', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateY(8px)' }),
        animate('250ms ease-out')
      ])
    ])
  ],
  templateUrl: './send-money.component.html',
  styleUrls: ['./send-money.component.scss']
})
export class SendMoneyComponent implements OnInit, OnDestroy {
  allUsers: any[] = [];
  filteredUsers: any[] = [];
  searchQuery = '';
  selectedUser: any = null;
  amount: number | null = null;
  description = '';
  selectedTag = '';
  password = '';
  showPassword = false;
  errorMessage = '';
  currentUser: any = null;

  isProcessing = false;
  isSuccess = false;
  transactionId: string = '';
  countdown = 5;
  progressWidth = 0;
  private countdownSubscription?: Subscription;

  tags = ['Bills', 'Restaurant', 'Shopping', 'Transfer', 'Other'];

  // Map frontend tags to backend transaction types
  private tagToTransactionType: { [key: string]: string } = {
    'Bills': 'BILL',
    'Restaurant': 'FOOD',
    'Shopping': 'SHOPPING',
    'Transfer': 'TRANSFER',
    'Other': 'OTHER'
  };

  constructor(
    private postService: PostService,
    private router: Router,
    private authService: AuthService,
    private rewardService: RewardService
  ) {}

  ngOnInit() {
    // Get current user from AuthService
    const user = this.authService.getCurrentUser();
    if (user) {
      this.currentUser = user;
      console.log('Current user:', this.currentUser);
    }

   
  }
  

  onSearchChange() {

    if (!this.searchQuery.trim()) {
      this.filteredUsers = [];
      return;
    }

    if (this.searchQuery.length < 3) {
      this.filteredUsers = [];
      return;
    }

    this.postService
      .searchUsers(this.searchQuery)
      .subscribe({
        next: users => {

          this.filteredUsers = users.filter(
            user => user.name !== this.currentUser.name
          );

        },
        error: err => {
          console.error(err);
        }
      });
  }

  onUserSelected(user: any) {
    this.selectedUser = user;
    // Set the search query to display the selected user
    this.searchQuery = `${user.name} (${user.vpaId || user.phoneNumber})`;
    this.filteredUsers = [];
    console.log('Selected user:', user);
  }

  /**
   * Generate a unique idempotency key using timestamp and random string
   * Format: userId_timestamp_randomString
   */
  private generateIdempotencyKey(): string {
    const timestamp = Date.now();
    const randomStr = Math.random().toString(36).substring(2, 15);
    const userId = this.currentUser?.id || 'unknown';
    return `${userId}_${timestamp}_${randomStr}`;
  }

  makePayment() {
    // Clear previous errors
    this.errorMessage = '';

    // Validate inputs
    if (!this.selectedUser) {
      this.errorMessage = 'Please select a recipient';
      return;
    }

    if (!this.amount || this.amount <= 0) {
      this.errorMessage = 'Please enter a valid amount';
      return;
    }

    if (!this.password || this.password.length < 6) {
      this.errorMessage = 'Please enter a password (minimum 6 characters)';
      return;
    }

    // Check if current user has VPA
    if (!this.currentUser?.vpaId && !this.currentUser?.vpa?.vpaId) {
      this.errorMessage = 'Your account does not have a VPA ID. Please contact support.';
      return;
    }

    // Check if recipient has VPA
    if (!this.selectedUser?.vpaId && !this.selectedUser?.vpa?.vpaId) {
      this.errorMessage = 'Recipient does not have a VPA ID';
      return;
    }

    // Get VPA IDs (handle both direct vpaId and nested vpa.vpaId)
    const payerVpaId = this.currentUser.vpaId || this.currentUser.vpa?.vpaId;
    const payeeVpaId = this.selectedUser.vpaId || this.selectedUser.vpa?.vpaId;

    // Generate idempotency key
    const idempotencyKey = this.generateIdempotencyKey();

    // Map the selected tag to transaction type
    const transactionType = this.selectedTag 
      ? this.tagToTransactionType[this.selectedTag] || 'OTHER'
      : 'OTHER';

    // Prepare payment request
    const note = (this.description || '').substring(0, 100);

    const paymentRequest = {
      payerVpaId: payerVpaId,
      payeeVpaId: payeeVpaId,
      payerPwd: this.password,
      idempotencyKey: idempotencyKey,
      amount: this.amount,
      note: note,
      transactionType: transactionType
    };

    console.log('Payment Request:', paymentRequest);

    this.isProcessing = true;
    this.isSuccess = true;

    // Make actual API call
    this.postService.makePayment(paymentRequest).subscribe({
      next: (response) => {
        console.log('Payment Response:', response);

        // Check if payment was successful
        if (response.status === 'SUCCESS') {
          
          this.rewardService.refreshRewardCount(); // Update reward indicator in navbar
          // Wait 1 second before showing success state
          setTimeout(() => {
            this.isProcessing = false;
            this.startCountdown();
          }, 1000);
        } else {
          // Payment failed
          this.errorMessage = response.failureReason || 'Payment failed. Please try again.';
          this.isSuccess = false;
          this.isProcessing = false;
          console.error('Payment failed:', response);
        }
      },
      error: (err) => {
        console.error('Payment API Error:', err);
        this.isProcessing = false;
        this.isSuccess = false;
        
        // Handle different types of errors
        if(err.status === 0) {
          this.errorMessage = 'Server is currently down. Please try later.';
        }
        else if(err.error?.includes("Account not linked")){
          this.errorMessage = this.selectedUser.name + " doesn't have a linked bank account.";
        }else{
          this.errorMessage = err.error;
        }
      }
    });
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

  resetForm() {
    this.searchQuery = '';
    this.selectedUser = null;
    this.amount = null;
    this.description = '';
    this.selectedTag = '';
    this.password = '';
    this.errorMessage = '';
    this.isSuccess = false;
    this.isProcessing = false;
    this.transactionId = '';
    this.filteredUsers = [];
    this.countdown = 5;
    this.progressWidth = 0;
    
    // Clean up countdown if active
    if (this.countdownSubscription) {
      this.countdownSubscription.unsubscribe();
    }
  }

  ngOnDestroy(): void {
    // Clean up subscription
    if (this.countdownSubscription) {
      this.countdownSubscription.unsubscribe();
    }
  }
}
