import { Component } from '@angular/core';
import { AuthService } from '../auth/auth.service';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { NavbarComponent } from '../navbar/navbar.component';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    CommonModule,
    NavbarComponent,
    MatCardModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent {
  user: any;

  constructor(private authService: AuthService) {
    this.user = this.authService.getCurrentUser() || {};
  }

  // Mask account number - show last 4 digits
  getAccountNumber(): string {
    if (!this.user.bankAccount?.accountNumber) return 'Not linked';
    const accNum = this.user.bankAccount.accountNumber;
    return `${accNum}`;
  }

  // Get status badge color
  getStatusColor(): string {
    switch (this.user.appStatus) {
      case 'ACTIVE': return 'active';
      case 'LOCKED': return 'locked';
      case 'CLOSED': return 'closed';
      default: return 'pending';
    }
  }

  // Get status display text
  getStatusText(): string {
    switch (this.user.appStatus) {
      case 'ACTIVE': return 'Active';
      case 'LOCKED': return 'Locked';
      case 'CLOSED': return 'Closed';
      default: return 'Pending';
    }
  }

  // Get user initials for avatar
  getInitials(): string {
    if (!this.user.name) return '?';
    const names = this.user.name.split(' ');
    if (names.length >= 2) {
      return names[0].charAt(0) + names[1].charAt(0);
    }
    return this.user.name.charAt(0);
  }
}