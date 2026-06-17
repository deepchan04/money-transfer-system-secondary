import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { PostService } from '../../service/post.service';
import { MatIconModule } from '@angular/material/icon';

@Component({
    selector: 'app-admin-login',
    standalone: true,
    imports: [
        CommonModule,
        MatCardModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatIconModule,
        FormsModule
    ],
    templateUrl: './admin-login.component.html',
    styleUrls: ['./admin-login.component.scss']
})
export class AdminLoginComponent {
    phoneNumber = '';
    password = '';
    errorMessage = '';
    showPassword = false;

    constructor(private postService: PostService, private router: Router) { }

    login() {
        // Clear previous errors
        this.errorMessage = '';

        // Check if user is already logged in from another tab
        const existingUser = localStorage.getItem('user');
        if (existingUser) {
            console.log('Admin already logged in from another tab, redirecting to admin dashboard');
            this.router.navigate(['/admin-dashboard']);
            return;
        }

        // Validate password length
        if (this.password.length < 6) {
            this.errorMessage = 'Password must be at least 6 characters long';
            return;
        }

        if (this.phoneNumber && this.password) {
            const loginData = {
                phoneNumber: this.phoneNumber,
                password: this.password
            };

            this.postService.loginUser(loginData).subscribe({
                next: (response) => {
                    console.log('Admin login successful', response);
                    if (response.token) {
                        localStorage.setItem('token', response.token);

                        // Fetch user details
                        this.postService.findByPhone(this.phoneNumber).subscribe({
                            next: (user) => {
                                console.log('Admin details fetched:', user);
                                localStorage.setItem('user', JSON.stringify(user));
                                // Redirect to admin dashboard
                                this.router.navigate(['/admin-dashboard']);
                            },
                            error: (err) => {
                                console.error('Error fetching admin details:', err);
                                this.errorMessage = err.error?.message || 'Failed to fetch admin details';
                                this.router.navigate(['/admin-dashboard']); // Navigate anyway
                            }
                        });
                    } else {
                        this.router.navigate(['/admin-dashboard']);
                    }
                },
                error: (err) => {
                    console.error('Admin login failed', err);
                    this.errorMessage = err.error?.message || 'Invalid credentials. Please try again.';
                }
            });
        }
    }

    goHome() {
    this.router.navigate(['/']); // Navigates to the home page (usually the root route)
  }
}
