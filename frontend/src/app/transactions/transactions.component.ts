import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { NavbarComponent } from '../navbar/navbar.component';
import { PostService } from '../service/post.service';

interface Transaction {
  date: string;
  time: string;
  primaryText: string;
  secondaryText: string;
  type: 'credit' | 'debit';
  amount: string;
  amountNumeric: number;
  points?: number;
  status: string;
  transactionTime: string;
  vpaId: string;
  note: string;
}

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NavbarComponent,
    MatTableModule,
    MatCardModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatButtonModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './transactions.component.html',
  styleUrls: ['./transactions.component.scss']
})
export class TransactionsComponent implements OnInit {
  displayedColumns = ['date', 'description', 'points', 'amount', 'status'];
  transactions: Transaction[] = [];
  filteredTransactions: Transaction[] = [];
  isLoading = false;
  user: any;

  // Filter properties
  searchQuery = '';
  typeFilter = 'all';
  sortBy = 'date-desc';
  selectedDate: Date | null = null;

  // Stats
  totalCredits = 0;
  totalDebits = 0;
  netBalance = 0;

  constructor(private postService: PostService) {
    const userData = sessionStorage.getItem('user');
    if (userData) {
      this.user = JSON.parse(userData);
    }
  }

  ngOnInit() {
    if (this.user?.vpa?.vpaId) {
      this.loadTransactions();
    }
  }

  loadTransactions() {
    this.isLoading = true;
    this.postService.getTransactionLog(this.user.vpa.vpaId).subscribe({
      next: (response) => {
        const allTransactions: Transaction[] = [];
        let creditsTotal = 0;
        let debitsTotal = 0;

        // Process credits
        if (response.credits && Array.isArray(response.credits)) {
          for (const credit of response.credits) {
            if (credit.status === 'SUCCESS') {
              creditsTotal += credit.amount;
            }
            
            const username = credit.payerVpaId;
            allTransactions.push({
              date: this.formatDate(credit.transactionTime),
              time: this.formatTime(credit.transactionTime),
              primaryText: `Received from ${username}`,
              secondaryText: credit.note || 'No description',
              points: credit.points ?? 0,
              amount: `₹${credit.amount.toLocaleString('en-IN')}`,
              amountNumeric: credit.amount,
              status: credit.status,
              type: 'credit',
              transactionTime: credit.transactionTime,
              vpaId: credit.payerVpaId,
              note: credit.note || ''
            });
          }
        }

        // Process debits
        if (response.debits && Array.isArray(response.debits)) {
          for (const debit of response.debits) {
            if (debit.status === 'SUCCESS') {
              debitsTotal += debit.amount;
            }

            const username = debit.payeeVpaId;
            allTransactions.push({
              date: this.formatDate(debit.transactionTime),
              time: this.formatTime(debit.transactionTime),
              primaryText: `Sent to ${username}`,
              secondaryText: debit.note || 'No description',
              points: debit.points ?? 0,
              amount: `₹${debit.amount.toLocaleString('en-IN')}`,
              amountNumeric: debit.amount,
              status: debit.status,
              type: 'debit',
              transactionTime: debit.transactionTime,
              vpaId: debit.payeeVpaId,
              note: debit.note || ''
            });
          }
        }

        this.transactions = allTransactions;
        this.totalCredits = creditsTotal;
        this.totalDebits = debitsTotal;
        this.netBalance = creditsTotal - debitsTotal;

        this.applyFilters();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading transactions:', error);
        this.isLoading = false;
        this.transactions = [];
        this.filteredTransactions = [];
      }
    });
  }

  applyFilters() {
    let filtered = [...this.transactions];

    // Apply search filter
    if (this.searchQuery && this.searchQuery.trim()) {
      const query = this.searchQuery.toLowerCase().trim();
      filtered = filtered.filter(tx => 
        tx.primaryText.toLowerCase().includes(query) ||
        tx.secondaryText.toLowerCase().includes(query) ||
        tx.vpaId.toLowerCase().includes(query) ||
        tx.note.toLowerCase().includes(query)
      );
    }

    // Apply type filter
    if (this.typeFilter !== 'all') {
      filtered = filtered.filter(tx => tx.type === this.typeFilter);
    }

    // Apply date filter
    if (this.selectedDate) {
      const selectedDateStr = this.formatDate(this.selectedDate.toISOString());
      filtered = filtered.filter(tx => tx.date === selectedDateStr);
    }

    // Apply sorting
    filtered = this.sortTransactions(filtered);

    this.filteredTransactions = filtered;
  }

  sortTransactions(transactions: Transaction[]): Transaction[] {
    switch (this.sortBy) {
      case 'date-desc':
        return transactions.sort((a, b) => 
          new Date(b.transactionTime).getTime() - new Date(a.transactionTime).getTime()
        );
      case 'date-asc':
        return transactions.sort((a, b) => 
          new Date(a.transactionTime).getTime() - new Date(b.transactionTime).getTime()
        );
      case 'amount-desc':
        return transactions.sort((a, b) => b.amountNumeric - a.amountNumeric);
      case 'amount-asc':
        return transactions.sort((a, b) => a.amountNumeric - b.amountNumeric);
      default:
        return transactions;
    }
  }

  clearFilters() {
    this.searchQuery = '';
    this.typeFilter = 'all';
    this.sortBy = 'date-desc';
    this.selectedDate = null;
    this.applyFilters();
  }

  hasActiveFilters(): boolean {
    return !!(this.searchQuery || this.typeFilter !== 'all' || this.selectedDate);
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    const day = date.getDate().toString().padStart(2, '0');
    const month = date.toLocaleString('en-US', { month: 'short' });
    const year = date.getFullYear();
    return `${day} ${month} ${year}`;
  }

  formatTime(dateString: string): string {
    const date = new Date(dateString);
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    return `${hours}:${minutes}`;
  }

  extractUsername(vpaId: string): string {
    // Extract username from VPA (e.g., "g12342@mts" -> "g12342")
    return vpaId.split('@')[0];
  }

  getStatusIcon(status: string): string {
    switch (status) {
      case 'SUCCESS':
        return 'check_circle';
      case 'FAILED':
        return 'cancel';
      case 'PENDING':
        return 'schedule';
      default:
        return 'help';
    }
  }
}