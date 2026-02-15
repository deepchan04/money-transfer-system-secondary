import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatSelectModule } from '@angular/material/select';
import { NavbarComponent } from '../../navbar/navbar.component';
import { PostService } from '../../service/post.service';

interface Transaction {
    id: number;
    from: string;
    to: string;
    amount: number;
    status: 'SUCCESS' | 'FAILED' | 'PENDING';
    transactionTime: string;
    note: string;
}

interface ApiUser {
    id: number;
    name: string;
    email: string;
    phoneNumber: string;
    appStatus: string | null;
    role: string;
    vpa: {
        id: number;
        vpaId: string;
    } | null;
    bankAccount: {
        id: number;
        accountNumber: string;
        balance: number;
        accountPassword: string;
        version: number;
    } | null;
}

interface User {
    id: string;
    name: string;
    email: string;
    phoneNumber: string;
    vpaId: string;
    accountStatus: string;
    accountNumber: string;
    balance: number;
}

interface PeakHour {
    TX_HOUR: number;
    TX_COUNT: number;
}

interface DailyVolume {
    TX_DATE: string;
    TOTAL_COUNT: number;
    TOTAL_AMOUNT: number;
}

interface ActiveAccount {
    ACCOUNT_ID: string;
    ACTIVITY_COUNT: number;
}

@Component({
    selector: 'app-admin-dashboard',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        RouterModule,
        NavbarComponent,
        MatCardModule,
        MatButtonModule,
        MatIconModule,
        MatInputModule,
        MatFormFieldModule,
        MatTableModule,
        MatChipsModule,
        MatSelectModule
    ],
    templateUrl: './admin-dashboard.component.html',
    styleUrls: ['./admin-dashboard.component.scss']
})
export class AdminDashboardComponent implements OnInit {
    searchQuery = '';
    isLoading = false;
    isLoadingTransactions = false;
    isLoadingAnalytics = false;
    errorMessage = '';

    // Statistics - will be calculated from API data
    stats = {
        totalUsers: 0,
        activeUsers: 0,
        blockedUsers: 0,
        totalTransactions: 0,
        todayTransactions: 0,
        totalVolume: 0,
        todayVolume: 0
    };

    // Analytics data from Snowflake
    successRate: number = 0;
    peakHours: PeakHour[] = [];
    dailyVolume: DailyVolume[] = [];
    averageAmount: number = 0;
    activeAccounts: ActiveAccount[] = [];

    // Real transactions from API
    recentTransactions: Transaction[] = [];

    // Real users from API
    users: User[] = [];
    filteredUsers: User[] = [];

    // Display only first 10 for dashboard
    recentTransactionsDisplay: Transaction[] = [];
    usersDisplay: User[] = [];

    displayedColumns: string[] = ['id', 'from', 'to', 'amount', 'status', 'date'];
    userColumns: string[] = ['id', 'name', 'email', 'phoneNumber', 'vpaId', 'accountStatus', 'accountNumber', 'balance'];

    // Status options
    statusOptions = ['ACTIVE', 'CLOSED', 'LOCKED'];

    constructor(private postService: PostService) { }

    ngOnInit() {
        this.loadUsers();
        this.loadTransactions();
        this.loadAnalytics();
    }

    loadAnalytics() {
        this.isLoadingAnalytics = true;

        // Load all analytics data
        this.postService.getSuccessRate().subscribe({
            next: (data) => {
                this.successRate = data.SUCCESS_RATE;
            },
            error: (err) => console.error('Error loading success rate:', err)
        });

        this.postService.getPeakHours().subscribe({
            next: (data) => {
                this.peakHours = data.sort((a: PeakHour, b: PeakHour) => b.TX_COUNT - a.TX_COUNT);
            },
            error: (err) => console.error('Error loading peak hours:', err)
        });

        this.postService.getDailyVolume().subscribe({
            next: (data) => {
                this.dailyVolume = data.sort((a: DailyVolume, b: DailyVolume) => 
                    new Date(a.TX_DATE).getTime() - new Date(b.TX_DATE).getTime()
                );
            },
            error: (err) => console.error('Error loading daily volume:', err)
        });

        this.postService.getAverageAmount().subscribe({
            next: (data) => {
                this.averageAmount = data.AVG_TRANSACTION_VALUE;
            },
            error: (err) => console.error('Error loading average amount:', err)
        });

        this.postService.getActiveAccounts().subscribe({
            next: (data) => {
                this.activeAccounts = data.sort((a: ActiveAccount, b: ActiveAccount) => 
                    b.ACTIVITY_COUNT - a.ACTIVITY_COUNT
                ).slice(0, 5); // Top 5 most active
                this.isLoadingAnalytics = false;
            },
            error: (err) => {
                console.error('Error loading active accounts:', err);
                this.isLoadingAnalytics = false;
            }
        });
    }

    loadUsers() {
        this.isLoading = true;
        this.errorMessage = '';

        this.postService.getAdminUsers().subscribe({
            next: (apiUsers: ApiUser[]) => {
                // Filter out admin users and transform data
                const regularUsers = apiUsers.filter(user => user.role !== 'ROLE_ADMIN');
                
                this.users = regularUsers.map(user => this.transformUser(user));
                this.filteredUsers = [...this.users];
                this.usersDisplay = this.users.slice(0, 5);

                // Calculate statistics
                this.calculateStats(regularUsers);

                this.isLoading = false;
                console.log('Users loaded:', this.users);
                console.log('Stats:', this.stats);
            },
            error: (err) => {
                console.error('Error loading users:', err);
                this.errorMessage = 'Failed to load users. Please try again.';
                this.isLoading = false;
            }
        });
    }

    loadTransactions() {
        this.isLoadingTransactions = true;

        this.postService.getAdminTransactionLog().subscribe({
            next: (apiTransactions: any[]) => {
                // Transform and sort transactions
                this.recentTransactions = apiTransactions
                    .map(txn => this.transformTransaction(txn))
                    .sort((a, b) => new Date(b.transactionTime).getTime() - new Date(a.transactionTime).getTime());

                // Take top 10 for display
                this.recentTransactionsDisplay = this.recentTransactions.slice(0, 10);

                // Calculate transaction statistics
                this.calculateTransactionStats(apiTransactions);

                this.isLoadingTransactions = false;
                console.log('Transactions loaded:', this.recentTransactions);
                console.log('Transaction stats:', {
                    total: this.stats.totalTransactions,
                    today: this.stats.todayTransactions,
                    totalVolume: this.stats.totalVolume,
                    todayVolume: this.stats.todayVolume
                });
            },
            error: (err) => {
                console.error('Error loading transactions:', err);
                this.isLoadingTransactions = false;
                this.recentTransactions = [];
                this.recentTransactionsDisplay = [];
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

    calculateTransactionStats(transactions: any[]) {
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        // Total transactions
        this.stats.totalTransactions = transactions.length;

        // Today's transactions
        this.stats.todayTransactions = transactions.filter(txn => {
            const txnDate = new Date(txn.transactionTime);
            txnDate.setHours(0, 0, 0, 0);
            return txnDate.getTime() === today.getTime();
        }).length;

        // Total volume (sum of all successful transactions)
        this.stats.totalVolume = transactions
            .filter(txn => txn.status === 'SUCCESS')
            .reduce((sum, txn) => sum + txn.amount, 0);

        // Today's volume
        this.stats.todayVolume = transactions
            .filter(txn => {
                const txnDate = new Date(txn.transactionTime);
                txnDate.setHours(0, 0, 0, 0);
                return txnDate.getTime() === today.getTime() && txn.status === 'SUCCESS';
            })
            .reduce((sum, txn) => sum + txn.amount, 0);
    }

    transformUser(apiUser: ApiUser): User {
        return {
            id: apiUser.id.toString(),
            name: apiUser.name,
            email: apiUser.email,
            phoneNumber: apiUser.phoneNumber,
            vpaId: apiUser.vpa?.vpaId || 'N/A',
            accountStatus: apiUser.appStatus || 'N/A',
            accountNumber: apiUser.bankAccount?.accountNumber || 'N/A',
            balance: apiUser.bankAccount?.balance || 0
        };
    }

    calculateStats(apiUsers: ApiUser[]) {
        // Total users (excluding admins)
        this.stats.totalUsers = apiUsers.length;
        
        // Active users - only those with 'ACTIVE' status
        this.stats.activeUsers = apiUsers.filter(user => user.appStatus === 'ACTIVE').length;
        
        // Blocked users - all users whose status is NOT 'ACTIVE'
        this.stats.blockedUsers = apiUsers.filter(user => user.appStatus !== 'ACTIVE').length;

        console.log('Total Users:', this.stats.totalUsers);
        console.log('Active Users:', this.stats.activeUsers);
        console.log('Blocked Users:', this.stats.blockedUsers);
        
        // Verification: totalUsers should equal activeUsers + blockedUsers
        const verification = this.stats.activeUsers + this.stats.blockedUsers;
        if (verification !== this.stats.totalUsers) {
            console.warn('Stats mismatch! Active + Blocked should equal Total');
        }
    }

    searchUsers() {
        if (!this.searchQuery.trim()) {
            this.filteredUsers = [...this.users];
            return;
        }

        const query = this.searchQuery.toLowerCase();
        this.filteredUsers = this.users.filter(user =>
            user.name.toLowerCase().includes(query) ||
            user.email.toLowerCase().includes(query) ||
            user.phoneNumber.includes(query) ||
            user.vpaId.toLowerCase().includes(query) ||
            user.id.toLowerCase().includes(query)
        );
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

    formatDateShort(dateString: string): string {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', { 
            month: 'short', 
            day: 'numeric'
        });
    }

    formatHour(hour: number): string {
        const period = hour >= 12 ? 'PM' : 'AM';
        const displayHour = hour === 0 ? 12 : hour > 12 ? hour - 12 : hour;
        return `${displayHour}${period}`;
    }

    getMaxPeakHourCount(): number {
        return Math.max(...this.peakHours.map(h => h.TX_COUNT), 1);
    }

    getMaxDailyVolume(): number {
        return Math.max(...this.dailyVolume.map(d => d.TOTAL_AMOUNT), 1);
    }

    getMaxActivityCount(): number {
        return Math.max(...this.activeAccounts.map(a => a.ACTIVITY_COUNT), 1);
    }

    // Generate SVG path for area chart
    generateAreaPath(): string {
        if (this.dailyVolume.length === 0) return '';
        
        const points = this.dailyVolume.map((d, i) => {
            const x = i * (600 / (this.dailyVolume.length - 1));
            const y = 200 - (d.TOTAL_AMOUNT / this.getMaxDailyVolume() * 150);
            return `L ${x} ${y}`;
        }).join(' ');
        
        const lastX = (this.dailyVolume.length - 1) * (600 / (this.dailyVolume.length - 1));
        return `M 0 200 ${points} L ${lastX} 200 Z`;
    }

    // Generate SVG points for area chart line
    generateLinePoints(): string {
        if (this.dailyVolume.length === 0) return '';
        
        return this.dailyVolume.map((d, i) => {
            const x = i * (600 / (this.dailyVolume.length - 1));
            const y = 200 - (d.TOTAL_AMOUNT / this.getMaxDailyVolume() * 150);
            return `${x},${y}`;
        }).join(' ');
    }

    // Get circle position for area chart
    getCircleX(index: number): number {
        if (this.dailyVolume.length <= 1) return 0;
        return index * (600 / (this.dailyVolume.length - 1));
    }

    getCircleY(amount: number): number {
        return 200 - (amount / this.getMaxDailyVolume() * 150);
    }

    // Get user name and ID by account ID
    getUserDisplayName(accountId: string): string {
        // The ACCOUNT_ID from Snowflake is the bank account ID, which matches user.id
        const user = this.users.find(u => u.id === accountId);
        if (user) {
            return `${user.name} (${user.accountNumber})`;
        }
        return `Account ${accountId}`;
    }

    changeUserStatus(user: User, newStatus: string) {
        // Don't do anything if user doesn't have VPA
        if (user.vpaId === 'N/A') {
            return;
        }

        const previousStatus = user.accountStatus;
        
        // Update UI immediately for better UX (optimistic update)
        user.accountStatus = newStatus;
        
        // Recalculate stats optimistically
        this.recalculateStatsAfterStatusChange(previousStatus, newStatus);
        
        this.postService.changeUserStatus(user.vpaId, newStatus).subscribe({
            next: (response) => {
                console.log('Status changed successfully:', response);
                console.log('Updated stats:', this.stats);
                
                // Reload users to ensure consistency
                this.postService.getAdminUsers().subscribe({
                    next: (apiUsers: ApiUser[]) => {
                        const regularUsers = apiUsers.filter(user => user.role !== 'ROLE_ADMIN');
                        
                        this.users = regularUsers.map(user => this.transformUser(user));
                        this.filteredUsers = [...this.users];
                        this.usersDisplay = this.users.slice(0, 5);

                        this.calculateStats(regularUsers);

                        console.log('Users reloaded:', this.users);
                        console.log('Stats:', this.stats);
                    },
                    error: (err) => {
                        console.error('Error reloading users:', err);
                    }
                });
            },
            error: (err) => {
                console.error('Error changing user status:', err);
                this.errorMessage = `Failed to change status for ${user.name}. Please try again.`;
                
                // Revert the UI change and stats on error
                user.accountStatus = previousStatus;
                this.recalculateStatsAfterStatusChange(newStatus, previousStatus);
            }
        });
    }

    recalculateStatsAfterStatusChange(oldStatus: string, newStatus: string) {
        // If changing from ACTIVE to non-ACTIVE
        if (oldStatus === 'ACTIVE' && newStatus !== 'ACTIVE') {
            this.stats.activeUsers--;
            this.stats.blockedUsers++;
        }
        // If changing from non-ACTIVE to ACTIVE
        else if (oldStatus !== 'ACTIVE' && newStatus === 'ACTIVE') {
            this.stats.activeUsers++;
            this.stats.blockedUsers--;
        }
    }
}