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
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { NavbarComponent } from '../../navbar/navbar.component';
import { PostService } from '../../service/post.service';

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
}

@Component({
    selector: 'app-admin-users',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        NavbarComponent,
        MatCardModule,
        MatButtonModule,
        MatIconModule,
        MatInputModule,
        MatFormFieldModule,
        MatTableModule,
        MatChipsModule,
        MatPaginatorModule,
        MatSelectModule,
        MatProgressSpinnerModule
    ],
    templateUrl: './admin-users.component.html',
    styleUrls: ['./admin-users.component.scss']
})
export class AdminUsersComponent implements OnInit {
    searchQuery = '';
    isLoading = false;
    errorMessage = '';

    allUsers: User[] = [];
    filteredUsers: User[] = [];
    paginatedUsers: User[] = [];

    displayedColumns: string[] = ['id', 'name', 'email', 'phoneNumber', 'vpaId', 'accountStatus', 'accountNumber'];

    // Status options
    statusOptions = ['ACTIVE', 'CLOSED', 'LOCKED'];

    // Pagination
    pageSize = 10;
    pageIndex = 0;
    totalItems = 0;

    constructor(private postService: PostService) { }

    ngOnInit() {
        window.scrollTo(0, 0);
        this.loadUsers();
    }

    loadUsers() {
        this.isLoading = true;
        this.errorMessage = '';

        this.postService.getAdminUsers().subscribe({
            next: (apiUsers: ApiUser[]) => {
                // Filter out admin users and transform data
                const regularUsers = apiUsers.filter(user => user.role !== 'ROLE_ADMIN');
                
                this.allUsers = regularUsers.map(user => this.transformUser(user));
                this.filteredUsers = [...this.allUsers];
                this.totalItems = this.filteredUsers.length;
                this.updatePagination();

                this.isLoading = false;
                console.log('Users loaded:', this.allUsers);
            },
            error: (err) => {
                console.error('Error loading users:', err);
                this.errorMessage = 'Failed to load users. Please try again.';
                this.isLoading = false;
            }
        });
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
        };
    }

    searchUsers() {
        if (!this.searchQuery.trim()) {
            this.filteredUsers = [...this.allUsers];
        } else {
            const query = this.searchQuery.toLowerCase();
            this.filteredUsers = this.allUsers.filter(user =>
                user.name.toLowerCase().includes(query) ||
                user.email.toLowerCase().includes(query) ||
                user.phoneNumber.includes(query) ||
                user.vpaId.toLowerCase().includes(query) ||
                user.id.toLowerCase().includes(query)
            );
        }
        this.totalItems = this.filteredUsers.length;
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
        this.paginatedUsers = this.filteredUsers.slice(startIndex, endIndex);
    }

    changeUserStatus(user: User, newStatus: string) {
        // Don't do anything if status hasn't changed or user doesn't have VPA
        if (user.accountStatus === newStatus || user.vpaId === 'N/A') {
            return;
        }

        const previousStatus = user.accountStatus;
        
        this.postService.changeUserStatus(user.vpaId, newStatus).subscribe({
            next: (response) => {
                console.log('Status changed successfully:', response);
                // Reload users to get fresh data from server
                this.loadUsers();
            },
            error: (err) => {
                console.error('Error changing user status:', err);
                this.errorMessage = `Failed to change status for ${user.name}. Please try again.`;
                // Optionally revert the UI change if it was optimistic
                user.accountStatus = previousStatus;
            }
        });
    }
}