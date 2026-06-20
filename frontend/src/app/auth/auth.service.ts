import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth'; // Example API URL

  private currentUser: any = null;

  constructor(private http: HttpClient) { }

  // Login method
  login(username: string, password: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/login`, { username, password });
  }

  // Signup method
  signup(username: string, email: string, password: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/signup`, { username, email, password });
  }

  // Save only JWT in localStorage and update in-memory user
  saveUserAndToken(user: any, token: string): void {
    this.currentUser = user;
    localStorage.setItem('token', token);
  }

  // Explicitly set the current user (e.g. after login)
  setCurrentUser(user: any): void {
    this.currentUser = user;
  }

  // Get current user from memory
  getCurrentUser(): any {
    return this.currentUser;
  }

  // Get JWT token from localStorage
  getToken(): string | null {
    return localStorage.getItem('token');
  }

  // Logout method to clear memory and localStorage
  logout(): void {
    this.currentUser = null;
    localStorage.removeItem('token');
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
      this.currentUser = null;
      return Promise.resolve(null);
    }

    const decoded = this.decodeToken(token);
    if (!decoded || !decoded.sub) {
      this.currentUser = null;
      return Promise.resolve(null);
    }

    const phoneNumber = decoded.sub;
    const url = `http://localhost:8080/users/findByPhone?phoneNumber=${phoneNumber}`;
    const headers = { 'Authorization': `Bearer ${token}` };
    
    return new Promise((resolve) => {
      this.http.get<any>(url, { headers }).subscribe({
        next: (user) => {
          this.currentUser = user;
          resolve(user);
        },
        error: (err) => {
          console.error('Error fetching user', err);
          this.currentUser = null;
          resolve(null);
        }
      });
    });
  }
}
