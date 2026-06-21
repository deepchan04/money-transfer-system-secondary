import { Component, HostListener, OnInit } from '@angular/core';
import { AuthService } from '../auth/auth.service';
import { CommonModule } from '@angular/common';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatBadgeModule } from '@angular/material/badge';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { Router, RouterModule, NavigationEnd } from '@angular/router';
import { PostService } from '../service/post.service';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [
    CommonModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatBadgeModule,
    RouterModule
  ],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit {
  userInitial = 'G';
  userRole = '';
  unscratchedRewardsCount = 0;
  currentUrl = '';
  isAccountActive = true;
  bankAccountLinked = false;
  user: any;
  navItems: any[] = [];

  constructor(private postService: PostService, private router: Router, private authService: AuthService) {
    this.loadUserData();
    
    // Listen to route changes to update user data
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      this.currentUrl = this.router.url;
      this.loadUserData();
    });
  }

  ngOnInit() {
    this.currentUrl = this.router.url;
    this.loadUserData();
  }

  loadUserData() {
    const user = this.authService.getCurrentUser();
    if (user) {
      this.userInitial = this.computeInitials(user.name);
      this.userRole = user.role || '';
      this.isAccountActive = user.appStatus === 'ACTIVE';
      this.bankAccountLinked = user.bankAccountLinked;
      this.loadRewardIndicator();
    } else {
      this.userRole = '';
      this.unscratchedRewardsCount = 0;
    }
  }

  // Compute initials similarly to ProfileComponent.getInitials()
  private computeInitials(name?: string): string {
    if (!name) return 'G';
    const parts = name.trim().split(/\s+/);
    if (parts.length >= 2) {
      return (parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
    }
    return parts[0].charAt(0).toUpperCase();
  }

  @HostListener('window:rewardsUpdated')
  loadRewardIndicator(): void {
    if (!this.isLoggedIn() || this.isAdmin()) {
      this.unscratchedRewardsCount = 0;
      return;
    }

    this.postService.getRewardStatus().subscribe({
      next: (status) => {
        this.unscratchedRewardsCount = status?.unscratchedCount ?? 0;
      },
      error: () => {
        this.unscratchedRewardsCount = 0;
      }
    });
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

  isAdmin(): boolean {
    const user = this.authService.getCurrentUser();
    if (user) {
      return user.role === 'ROLE_ADMIN';
    }
    return false;
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}
