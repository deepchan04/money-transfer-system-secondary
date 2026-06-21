import { Injectable, signal} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {AuthSyncService} from "../service/authSyncService";
import { BehaviorSubject, tap } from 'rxjs';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth'; // Example API URL

  private currentUserSubject = new BehaviorSubject<any>(null);

  currentUser$ = this.currentUserSubject.asObservable();

  constructor(private router: Router, private http: HttpClient, private authSyncService: AuthSyncService) { 
      this.authSyncService.tokenReceived$
    .subscribe(token => {

      if (!sessionStorage.getItem('token')) {

        sessionStorage.setItem('token', token);

        this.loadCurrentUser().then((user) => {
         if (user) {
          if(user.role === "ROLE_ADMIN") {
            this.router.navigate(['/admin-dashboard']);
          } else {
            this.router.navigate(['/dashboard']);
          }
        }
      });
      }
    });
    this.authSyncService.login$
  .subscribe(token => {

    this.loadCurrentUser().then(user => {

      if (user) {
        this.router.navigate(['/dashboard']);
      }

    });

  });
    this.authSyncService.logout$
      .subscribe(() => {
        this.currentUserSubject.next(null);
        this.router.navigate(['/']);
    });
  }

  

  // Login method
  login(username: string, password: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/login`, { username, password })
  
  }

  // Signup method
  signup(username: string, email: string, password: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/signup`, { username, email, password });
  }

  // Save only JWT in sessionStorage and update in-memory user
  saveUserAndToken(user: any, token: string): void {
    this.currentUserSubject.next(user);
    sessionStorage.setItem('token', token);
    this.authSyncService.broadcastLogin(token);
  }

  // Explicitly set the current user (e.g. after login)
  setCurrentUser(user: any): void {
    this.currentUserSubject.next(user);
  }

  // Get current user from memory
  getCurrentUser(): any {
    return this.currentUserSubject.value;
  }

  // Get JWT token from sessionStorage
  getToken(): string | null {
    return sessionStorage.getItem('token');
  }

  // Logout method to clear memory and sessionStorage
  logout(): void {
    this.currentUserSubject.next(null);
    sessionStorage.removeItem('token');
    this.authSyncService.broadcastLogout();
    this.router.navigate(['/']);
  }

  // Decode JWT payload
  private decodeToken(token: string): any {
    try {
      const payload = token.split('.')[1];
      const decoded = atob(payload);
      return JSON.parse(decoded);
    } catch (e) {
      console.error('Error decoding token', e);
      return null;
    }
  }

  // Load user data based on token
  loadCurrentUser(): Promise<any> {
    
    const token = this.getToken();
    
    if (!token) {
      this.currentUserSubject.next(null);
      this.authSyncService.requestToken();
      return Promise.resolve(null);
    }

    const decoded = this.decodeToken(token);
    if (!decoded || !decoded.sub) {
      this.currentUserSubject.next(null);
      return Promise.resolve(null);
    }

    const phoneNumber = decoded.sub;
    const url = `http://localhost:8080/users/findByPhone?phoneNumber=${phoneNumber}`;
    const headers = { 'Authorization': `Bearer ${token}` };
    
    return new Promise((resolve) => {
      this.http.get<any>(url, { headers }).subscribe({
        next: (user) => {
          this.currentUserSubject.next(user);
          resolve(user);
        },
        error: (err) => {
          this.currentUserSubject.next(null);
          resolve(null);
        }
      });
    });
  }
}
