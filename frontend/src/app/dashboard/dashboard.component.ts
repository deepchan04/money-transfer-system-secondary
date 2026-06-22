import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { RouterModule } from '@angular/router';
import { PostService } from '../services/post.service';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';


interface Transaction {
  date: string;
  description: string;
  amount: string;
  status: string;
  type: 'credit' | 'debit';
  transactionTime: string;
  numericAmount?: number;
  transactionType?: string;
  tag?: string;
}

import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    RouterModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  hasBankAccount = false;
  totalSpent = 0;
  currentMonthSpent = 0;
  user: any;
  transactions: Transaction[] = [];
  allTransactions: Transaction[] = [];
  isLoading = false;
  isAccountActive = true;
  moneySent = false;

  // Initialize with default values to prevent UI breaking
  spendingBreakdown = [
    { label: 'Direct Transfers', value: 55, color: '#3b82f6' },
    { label: 'Bill Payments', value: 25, color: '#8b5cf6' },
    { label: 'Subscriptions', value: 20, color: '#ec4899' }
  ];

  chartHeight = 180;

  monthlySpending = [
    { month: 'Jun', amount: 18200 },
    { month: 'Jul', amount: 10400 },
    { month: 'Aug', amount: 9600 },
    { month: 'Sep', amount: 13200 },
    { month: 'Oct', amount: 8210 },
    { month: 'Nov', amount: 7200 },
    { month: 'Dec', amount: 18000 },
    { month: 'Jan', amount: 20200 },
  ];

  maxMonthlySpend = Math.max(...this.monthlySpending.map(m => m.amount));

  constructor(private postService: PostService, private authService: AuthService) {
    this.user = this.authService.getCurrentUser();
    if (this.user) {
      this.hasBankAccount = !!this.user.bankAccount;
      

      // Check if account is active
      this.isAccountActive = this.user.appStatus === 'ACTIVE';
    }
  }

  ngOnInit() {
    if (this.hasBankAccount && this.isAccountActive && this.user.vpa.vpaId) {
      this.loadTransactions();
    }
  }

  getGreeting(): string {
    const hour = new Date().getHours();

    if (hour < 12) {
      return 'Morning';
    } else if (hour < 17) {
      return 'Afternoon';
    } else {
      return 'Evening';
    }
  }

  getStatusMessage(): string {
    if (!this.user?.appStatus) {
      return 'Your account status is pending verification.';
    }

    switch (this.user.appStatus) {
      case 'CLOSED':
        return 'Your account has been permanently closed. Please contact our support team to understand the reason and explore reinstatement options.';
      case 'LOCKED':
        return 'Your account has been temporarily locked for security reasons. Our team is reviewing your account.';
      default:
        return 'Your account is currently inactive. This may be due to pending verification or security checks.';
    }
  }

  // Calculate successful transaction count
  getSuccessfulCount(): number {
    return this.allTransactions.filter(t => t.status === 'SUCCESS' && t.type === 'debit').length;
  }

  // Calculate success rate percentage
  getSuccessRate(): string {
    if (this.allTransactions.length === 0) return '0';
    const rate = (this.getSuccessfulCount() / this.allTransactions.length) * 100;
    return rate.toFixed(0);
  }

  // Calculate average transaction amount
  getAverageTransaction(): string {
    if (!this.allTransactions || this.allTransactions.length === 0) return '0';

    // Compute average only across successful transactions
    const successfulTxns = this.allTransactions.filter(
      t => t.status === 'SUCCESS' && typeof t.numericAmount === 'number'
    );
    if (successfulTxns.length === 0) return '0';

    const total = successfulTxns.reduce((sum, t) => sum + (t.numericAmount || 0), 0);
    const average = total / successfulTxns.length;
    return Math.round(average).toLocaleString('en-IN', { maximumFractionDigits: 0 });
  }

  // Calculate current month spending
  calculateCurrentMonthSpent() {
    const now = new Date();
    const currentMonth = now.getMonth();
    const currentYear = now.getFullYear();

    // Filter successful debit transactions from current month
    const currentMonthDebits = this.allTransactions.filter(t => {
      if (t.type !== 'debit' || t.status !== 'SUCCESS') return false;

      const txnDate = new Date(t.transactionTime);
      return txnDate.getMonth() === currentMonth && txnDate.getFullYear() === currentYear;
    });

    // Sum up the amounts
    this.currentMonthSpent = currentMonthDebits.reduce((sum, t) => {
      return sum + (t.numericAmount || 0);
    }, 0);
  }

  loadTransactions() {
    this.isLoading = true;
    this.postService.getTransactionLog(this.user.vpa.vpaId).subscribe({
      next: (response) => {
        const allTransactions: any[] = [];

        // Collect all transactions with VPA IDs and transactionType
        if (response.credits && Array.isArray(response.credits)) {
          response.credits.forEach((credit: any) => {
            allTransactions.push({
              type: 'credit',
              vpaId: credit.payerVpaId,
              amount: credit.amount,
              note: credit.note,
              transactionType: credit.transactionType, // CAPTURE transactionType here
              status: credit.status,
              transactionTime: credit.transactionTime
            });
          });
        }

        if (response.debits && Array.isArray(response.debits)) {
          response.debits.forEach((debit: any) => {
            allTransactions.push({
              type: 'debit',
              vpaId: debit.payeeVpaId,
              amount: debit.amount,
              note: debit.note,
              transactionType: debit.transactionType, // CAPTURE transactionType here
              status: debit.status,
              transactionTime: debit.transactionTime
            });
          });
        }

        if (response.debits && Array.isArray(response.debits) && response.debits.length > 0) {
          this.moneySent = true;
        }

        // Transform transactions using VPA IDs
        const transformedTransactions: Transaction[] = allTransactions.map(txn => {
          const userName = txn.vpaId;
          
          return {
            date: this.formatDate(txn.transactionTime),
            description: txn.type === 'credit' 
              ? `Received from ${userName} ${txn.note ? '- ' + txn.note : ''}`
              : `Sent to ${userName} ${txn.note ? '- ' + txn.note : ''}`,
            amount: `₹${txn.amount.toLocaleString('en-IN')}`,
            status: txn.status,
            type: txn.type,
            transactionTime: txn.transactionTime,
            numericAmount: txn.amount,
            transactionType: txn.transactionType // PASS transactionType to transformed transaction
          };
        });

        // Store all transactions sorted by time
        this.allTransactions = transformedTransactions
          .sort((a, b) => new Date(b.transactionTime).getTime() - new Date(a.transactionTime).getTime());

        // Top 5 for display
        this.transactions = this.allTransactions.slice(0, 5);

        // Calculate current month spending
        this.calculateCurrentMonthSpent();

        // Generate spending breakdown from tags
        this.generateSpendingBreakdown();

        // Generate monthly spending data
        this.generateMonthlySpending();



        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading transactions:', error);
        this.isLoading = false;
        this.transactions = [];
        this.allTransactions = [];
      }
    });
  }

  generateSpendingBreakdown() {
    // Filter only successful debit transactions
    const debits = this.allTransactions.filter(
      t => t.type === 'debit' && t.status === 'SUCCESS'
    );

    if (debits.length === 0) {
      // Keep default values if no debits
      return;
    }

    // Group by tag and sum amounts
    const tagAmounts = new Map<string, number>();

    debits.forEach(txn => {
      const tag = txn.transactionType || 'Others';
      const current = tagAmounts.get(tag) || 0;
      tagAmounts.set(tag, current + (txn.numericAmount || 0));
    });

    // Calculate total
    const total = Array.from(tagAmounts.values()).reduce((sum, val) => sum + val, 0);

    if (total === 0) {
      return;
    }

    // Color palette for tags
    const colors = [
      '#3b82f6', // blue
      '#8b5cf6', // purple
      '#ec4899', // pink
      '#10b981', // green
      '#f59e0b', // amber
      '#ef4444', // red
      '#06b6d4', // cyan
      '#a855f7', // violet
    ];

    // Create breakdown array with percentages
    let breakdown = Array.from(tagAmounts.entries())
      .map(([tag, amount], index) => ({
        label: tag,
        value: Math.round((amount / total) * 100),
        color: colors[index % colors.length]
      }))
      .sort((a, b) => b.value - a.value); // Sort by value descending

    // Adjust to ensure total is 100%
    const totalPercentage = breakdown.reduce((sum, b) => sum + b.value, 0);
    if (totalPercentage !== 100 && breakdown.length > 0) {
      breakdown[0].value += (100 - totalPercentage);
    }

    // Ensure we always have at least 3 items for the UI (pad with 0% if needed)
    while (breakdown.length < 3) {
      breakdown.push({
        label: 'No Data',
        value: 0,
        color: '#1e293b'
      });
    }

    this.spendingBreakdown = breakdown;
  }

  generateMonthlySpending() {
    // Filter only successful debit transactions
    const debits = this.allTransactions.filter(
      t => t.type === 'debit' && t.status === 'SUCCESS'
    );

    if (debits.length === 0) {
      // Keep default values if no debits
      return;
    }

    // Group by month
    const monthlyData = new Map<string, number>();

    debits.forEach(txn => {
      const date = new Date(txn.transactionTime);
      const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
      const current = monthlyData.get(monthKey) || 0;
      monthlyData.set(monthKey, current + (txn.numericAmount || 0));
    });

    // Convert to array and sort by date
    const sortedMonths = Array.from(monthlyData.entries())
      .map(([key, amount]) => {
        const [year, month] = key.split('-');
        const date = new Date(parseInt(year), parseInt(month) - 1);
        return {
          month: date.toLocaleString('en-US', { month: 'short' }),
          amount: Math.round(amount),
          sortKey: key
        };
      })
      .sort((a, b) => a.sortKey.localeCompare(b.sortKey));

    // Take last 8 months
    this.monthlySpending = sortedMonths.slice(-8);

    // If we have data, calculate max for scaling
    if (this.monthlySpending.length > 0) {
      this.maxMonthlySpend = Math.max(...this.monthlySpending.map(m => m.amount), 1);
    }
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    const day = date.getDate().toString().padStart(2, '0');
    const month = date.toLocaleString('en-US', { month: 'short' });
    return `${day} ${month}`;
  }

  extractUsername(vpaId: string): string {
    return vpaId.split('@')[0];
  }

  getBarHeight(amount: number): number {
    if (this.maxMonthlySpend === 0) return 0;
    return (amount / this.maxMonthlySpend) * this.chartHeight;
  }

  getPieChartGradient(): string {
    if (this.spendingBreakdown.length === 0) {
      return `conic-gradient(#1e293b 0% 100%)`;
    }

    // Filter out items with 0% value
    const validItems = this.spendingBreakdown.filter(item => item.value > 0);

    if (validItems.length === 0) {
      return `conic-gradient(#1e293b 0% 100%)`;
    }

    let gradient = 'conic-gradient(';
    let currentPercentage = 0;

    validItems.forEach((item, index) => {
      const nextPercentage = currentPercentage + item.value;
      gradient += `${item.color} ${currentPercentage}% ${nextPercentage}%`;

      // Only add comma if not the last item
      if (index < validItems.length - 1) {
        gradient += ', ';
      }

      currentPercentage = nextPercentage;
    });

    gradient += ')';
    return gradient;
  }

  displayedColumns = ['date', 'description', 'amount', 'status'];
}