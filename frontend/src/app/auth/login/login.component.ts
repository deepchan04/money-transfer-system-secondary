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

import { AuthService } from '../auth.service';

@Component({
  selector: 'app-login',
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
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  phoneNumber = '';
  password = '';
  errorMessage = '';
  showPassword = false;
  phoneNonNumeric = false;

  onPhoneInput(value: string) {
    // Strip any non-digit characters and limit to 10 digits
    const raw = value || '';
    // mark if user entered any non-digit characters
    this.phoneNonNumeric = /\D/.test(raw);
    const digits = raw.replace(/\D+/g, '').slice(0, 10);
    if (digits !== this.phoneNumber) {
      this.phoneNumber = digits;
    }
  }

  constructor(private postService: PostService, private router: Router, private authService: AuthService) { }

  login() {
    // Clear previous errors
    this.errorMessage = '';
    

    // Frontend validations
    if (!this.phoneNumber || !this.password) {
      this.errorMessage = 'Please enter both phone number and password';
      return;
    }

    // Validate password length
    if (this.password.length < 6) {
      this.errorMessage = 'Password must be at least 6 characters long';
      return;
    }

    // Ensure phone number is exactly 10 digits
    if (this.phoneNumber.length !== 10) {
      this.errorMessage = 'Phone number must be 10 digits';
      return;
    }

    // Check if user is already logged in from another tab
    const existingToken = localStorage.getItem('token');
    if (existingToken) {
      console.log('User already logged in from another tab, redirecting to dashboard');
      this.router.navigate(['/dashboard']);
      return;
    }

    const loginData = {
      phoneNumber: this.phoneNumber,
      password: this.password
    };

    this.postService.loginUser(loginData).subscribe({
      next: (response) => {
        console.log('Login successful', response);
        
        if (!response.token) {
          this.errorMessage = 'Login failed. No authentication token received.';
          return;
        }

        localStorage.setItem('token', response.token);

        // Fetch user details
        this.postService.findByPhone(this.phoneNumber).subscribe({
          next: (user) => {
            console.log('User details fetched:', user);

            // Check if user account is closed
            if (user.appStatus === 'CLOSED') {
              this.errorMessage = 'Your account is closed. Please contact support.';
              localStorage.removeItem('token');
              this.authService.setCurrentUser(null);
              return;
            }

            // Save user details and navigate to dashboard
            this.authService.setCurrentUser(user);
            this.router.navigate(['/dashboard']);
          },
          error: (err) => {
            console.error('Error fetching user details:', err);
            this.errorMessage = 'Failed to fetch user details. Please try again.';
          }
        });
      },
      error: (err) => {
        console.error('Login failed', err);
        
        // Handle different error scenarios from backend
        if (err.status === 401) {
          this.errorMessage = 'Invalid phone number or password. Please try again.';
        } else if (err.status === 403) {
          this.errorMessage = 'Access denied. Your account may be inactive.';
        } else if (err.status === 404) {
          this.errorMessage = 'User not found. Please check your phone number.';
        } else if (err.status === 500) {
          this.errorMessage = 'Server error. Please try again later.';
        } else if (err.error?.message) {
          this.errorMessage = err.error.message;
        } else {
          this.errorMessage = 'Login failed. Please check your credentials and try again.';
        }
      }
    });
  }
}
