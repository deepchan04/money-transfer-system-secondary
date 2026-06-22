import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../auth.service';
import { PostService, UserData } from '../../service/post.service';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';

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
    FormsModule
  ],
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.scss']
})
export class SignupComponent {
  private readonly emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  private readonly nameRegex = /^[A-Za-z]+(?: [A-Za-z]+)*$/;

  name: string = '';
  email: string = '';
  password: string = '';
  confirmPassword: string = '';
  phoneNumber: string = '';
  errorMessage: string = '';
  showPassword = false;
  showConfirmPassword = false;
  phoneNonNumeric = false;
  nameNonAlphabetic = false;

  @Output() signupSuccess = new EventEmitter<void>();

  constructor(private postService: PostService, private authService: AuthService, private snackBar: MatSnackBar) { }

  onPhoneInput(value: string) {
    const raw = value || '';
    this.phoneNonNumeric = /\D/.test(raw);
    const digits = raw.replace(/\D+/g, '').slice(0, 10);

    if (digits !== this.phoneNumber) {
      this.phoneNumber = digits;
    }
  }

  onNameInput(value: string) {
    const raw = value || '';
    this.nameNonAlphabetic = /[^A-Za-z\s]/.test(raw);
    const sanitized = raw.replace(/[^A-Za-z\s]+/g, '').replace(/\s+/g, ' ');

    if (sanitized !== this.name) {
      this.name = sanitized;
    }
  }

  isNameValid(): boolean {
    const trimmedName = this.name.trim();
    return trimmedName.length >= 2 && this.nameRegex.test(trimmedName);
  }

  isEmailValid(): boolean {
    return this.emailRegex.test(this.email);
  }

  signup(): void {
    
    this.errorMessage = '';

    
    if (!this.name || !this.email || !this.password || !this.confirmPassword || !this.phoneNumber) {
      this.errorMessage = 'Please fill in all fields.';
      return;
    }

    
    if (!this.isNameValid()) {
      this.errorMessage = 'Name must be at least 2 characters long.';
      return;
    }

    
    if (!this.isEmailValid()) {
      this.errorMessage = 'Please enter a valid email address.';
      return;
    }

    
    if (this.phoneNumber.length !== 10) {
      this.errorMessage = 'Phone number must be 10 digits';
      return;
    }

    
    if (this.password.length < 6) {
      this.errorMessage = 'Password must be at least 6 characters long.';
      return;
    }

    
    if (this.password !== this.confirmPassword) {
      this.errorMessage = 'Passwords do not match!';
      return;
    }

    const newUser: UserData = {
      name: this.name.trim(),
      email: this.email,
      phoneNumber: this.phoneNumber,
      password: this.password
    };

    this.postService.createUser(newUser).subscribe({
      next: () => {
        
        this.snackBar.open(
          'Signup successful! Welcome aboard!',
          'Close',
          {
            duration: 5000,
            horizontalPosition: 'center',
            verticalPosition: 'top',
            panelClass: ['success-snackbar']
          }
        );
        this.signupSuccess.emit();
      },
      error: (err) => {
        
        if(err.status === 0) {
          this.errorMessage = 'Server is currently down. Please try later.';
          }
        else if (err.status === 400) {
          this.errorMessage = err.error?.message || 'Invalid input. Please check all fields.';
        } else if (err.status === 409) {
          const raw = (typeof err.error === 'string') ? err.error : (err.error?.message || '');
          const lowered = raw.toLowerCase();
          const hasPhone = lowered.includes('phone');
          const hasEmail = lowered.includes('email');
          if (hasPhone && hasEmail) {
            this.errorMessage = 'Both phone number and email are already registered.';
          } else if (hasPhone) {
            this.errorMessage = 'Phone number already registered.';
          } else if (hasEmail) {
            this.errorMessage = 'Email already registered.';
          } else {
            this.errorMessage = raw || 'An account with this phone number or email already exists.';
          }
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
