import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../auth.service';
import { PostService, UserData } from '../../service/post.service';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    HttpClientModule,
    FormsModule
  ],
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.scss']
})
export class SignupComponent {
  name: string = '';
  email: string = '';
  password: string = '';
  confirmPassword: string = '';
  phoneNumber: string = '';
  errorMessage: string = '';

  @Output() signupSuccess = new EventEmitter<void>();

  constructor(private postService: PostService) { }

  signup(): void {
    // Clear previous errors
    this.errorMessage = '';

    // Check for empty fields
    if (!this.name || !this.email || !this.password || !this.confirmPassword || !this.phoneNumber) {
      this.errorMessage = 'Please fill in all fields.';
      return;
    }

    // Validate name
    if (this.name.trim().length < 2) {
      this.errorMessage = 'Name must be at least 2 characters long.';
      return;
    }

    // Validate email format
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(this.email)) {
      this.errorMessage = 'Please enter a valid email address.';
      return;
    }

    // Validate phone number format (basic validation)
    const phoneRegex = /^[0-9]{10}$/;
    if (!phoneRegex.test(this.phoneNumber)) {
      this.errorMessage = 'Please enter a valid 10-digit phone number.';
      return;
    }

    // Validate password length
    if (this.password.length < 6) {
      this.errorMessage = 'Password must be at least 6 characters long.';
      return;
    }

    // Check password match
    if (this.password !== this.confirmPassword) {
      this.errorMessage = 'Passwords do not match!';
      return;
    }

    const newUser: UserData = {
      name: this.name,
      email: this.email,
      phoneNumber: this.phoneNumber,
      password: this.password
    };

    this.postService.createUser(newUser).subscribe({
      next: (response) => {
        console.log('User created successfully!', response);

        // Fetch user details by phone number
        this.postService.findByPhone(this.phoneNumber).subscribe({
          next: (user) => {
            console.log('User details fetched:', user);
            localStorage.setItem('user', JSON.stringify(user));
            alert('Signup successful! Welcome aboard!');
            this.signupSuccess.emit();
          },
          error: (err) => {
            console.log(err.error);
            
            console.error('Error fetching user details:', err);
            this.errorMessage = 'Signup successful, but failed to fetch user details. Please login.';
            this.signupSuccess.emit(); // Still emit success to proceed
          }
        });
      },
      error: (err) => {
        console.log(err);
        console.error('Error occurred:', err);
        
        // Handle different error scenarios from backend
        if (err.status === 400) {
          // Bad request - could be validation error
          this.errorMessage = err.error?.message || 'Invalid input. Please check all fields.';
        } else if (err.status === 409) {
          // Conflict - user already exists
          this.errorMessage = 'An account with this phone number or email already exists.';
        } else if (err.status === 500) {
          this.errorMessage = 'Server error. Please try again later.';
        } else if (err.error?.message) {
          this.errorMessage = err.error.message;
        } else {
          this.errorMessage = 'Signup failed. Please try again.';
        }
      }
    });
  }
}