import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { NavbarComponent } from '../navbar/navbar.component';
import { PostService } from '../service/post.service';
import { RouterModule } from '@angular/router';

@Component({
    selector: 'app-check-balance',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        MatCardModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatIconModule,
        NavbarComponent,
        RouterModule
    ],
    templateUrl: './check-balance.component.html',
    styleUrls: ['./check-balance.component.scss']
})
export class CheckBalanceComponent {
    password = '';
    balance: number | null = null;
    user: any;
    errorMessage = '';
    showPassword = false;

    constructor(private postService: PostService) {
        const userData = sessionStorage.getItem('user');
        if (userData) {
            this.user = JSON.parse(userData);
        }
    }

    fetchBalance() {
        // Clear previous errors
        this.errorMessage = '';

        if (!this.password) {
            return;
        }

        if (this.password.length < 6) {
            return;
        }

        const vpaId = this.user?.vpa?.vpaId;
        const password = this.password;

        this.postService.getBalance(vpaId, password).subscribe({
            next: (response) => {
                console.log('Balance fetched:', response);
                // Assuming response contains the balance directamente o en un campo balance
                this.balance = response.balance !== undefined ? response.balance : response;
            },
            error: (err) => {
                console.error('Error fetching balance:', err);
                this.errorMessage = err.error?.message || 'Failed to fetch balance. Please check your password.';
            }
        });
    }
}
