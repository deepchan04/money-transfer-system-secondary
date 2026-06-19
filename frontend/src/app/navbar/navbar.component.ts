import { Component, HostListener, OnInit } from '@angular/core';
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

  constructor(private postService: PostService, private router: Router) {
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
    const userData = localStorage.getItem('user');
    if (userData) {
      const user = JSON.parse(userData);
      this.userInitial = user.name ? user.name.charAt(0).toUpperCase() : 'G';
      this.userRole = user.role || '';
      this.loadRewardIndicator();
    } else {
      this.userRole = '';
      this.unscratchedRewardsCount = 0;
    }
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
    // Check user role from localStorage, not from URL
    const userData = localStorage.getItem('user');
    if (userData) {
      const user = JSON.parse(userData);
      return user.role === 'ROLE_ADMIN';
    }
    return false;
  }

  logout() {
    this.postService.logout();
    this.router.navigate(['/']);
  }
}
