import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { PostService } from '../../services/post.service';

interface Transaction {
    id: number;
    from: string;
    to: string;
    amount: number;
    status: 'SUCCESS' | 'FAILED' | 'PENDING';
    transactionTime: string;
    note: string;
}

@Component({
    selector: 'app-admin-transactions',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        MatCardModule,
        MatButtonModule,
        MatIconModule,
        MatInputModule,
        MatFormFieldModule,
        MatTableModule,
        MatChipsModule,
        MatPaginatorModule,
        MatProgressSpinnerModule
    ],
    templateUrl: './admin-transactions.component.html',
    styleUrls: ['./admin-transactions.component.scss']
})
export class AdminTransactionsComponent implements OnInit {
    searchQuery = '';
    isLoading = false;

    allTransactions: Transaction[] = [];
    filteredTransactions: Transaction[] = [];
    paginatedTransactions: Transaction[] = [];

    displayedColumns: string[] = ['id', 'from', 'to', 'amount', 'status', 'date'];

    // Pagination
    pageSize = 10;
    pageIndex = 0;
    totalItems = 0;

    constructor(private postService: PostService) {}

    ngOnInit() {
        window.scrollTo(0, 0);
        this.loadTransactions();
    }

    loadTransactions() {
        this.isLoading = true;

        this.postService.getAdminTransactionLog().subscribe({
            next: (apiTransactions: any[]) => {
                // Transform and sort transactions by time (latest first)
                this.allTransactions = apiTransactions
                    .map(txn => this.transformTransaction(txn))
                    .sort((a, b) => new Date(b.transactionTime).getTime() - new Date(a.transactionTime).getTime());

                this.filteredTransactions = [...this.allTransactions];
                this.totalItems = this.filteredTransactions.length;
                this.updatePagination();
                this.isLoading = false;

                console.log('All transactions loaded:', this.allTransactions.length);
            },
            error: (err) => {
                console.error('Error loading transactions:', err);
                this.isLoading = false;
                this.allTransactions = [];
                this.filteredTransactions = [];
                this.totalItems = 0;
            }
        });
    }

    transformTransaction(apiTxn: any): Transaction {
        return {
            id: apiTxn.id,
            from: apiTxn.payer?.name || 'Unknown',
            to: apiTxn.payee?.name || 'Unknown',
            amount: apiTxn.amount,
            status: apiTxn.status,
            transactionTime: apiTxn.transactionTime,
            note: apiTxn.note || ''
        };
    }

    searchTransactions() {
        if (!this.searchQuery.trim()) {
            this.filteredTransactions = [...this.allTransactions];
        } else {
            const query = this.searchQuery.toLowerCase();
            this.filteredTransactions = this.allTransactions.filter(txn =>
                txn.id.toString().includes(query) ||
                txn.from.toLowerCase().includes(query) ||
                txn.to.toLowerCase().includes(query) ||
                txn.note.toLowerCase().includes(query)
            );
        }
        this.totalItems = this.filteredTransactions.length;
        this.pageIndex = 0;
        this.updatePagination();
    }

    onPageChange(event: PageEvent) {
        this.pageSize = event.pageSize;
        this.pageIndex = event.pageIndex;
        this.updatePagination();
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    updatePagination() {
        const startIndex = this.pageIndex * this.pageSize;
        const endIndex = startIndex + this.pageSize;
        this.paginatedTransactions = this.filteredTransactions.slice(startIndex, endIndex);
    }

    formatDate(dateString: string): string {
        const date = new Date(dateString);
        return date.toLocaleString('en-US', { 
            month: 'short', 
            day: 'numeric', 
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
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