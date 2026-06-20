import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth'; // Example API URL

  constructor(private http: HttpClient) { }

  // Login method
  login(username: string, password: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/login`, { username, password });
  }

  // Signup method
  signup(username: string, email: string, password: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/signup`, { username, email, password });
  }

  // Save user and JWT in sessionStorage
  saveUserAndToken(user: any, token: string): void {
    sessionStorage.setItem('user', JSON.stringify(user));
    sessionStorage.setItem('token', token);
  }

  // Get current user from sessionStorage
  getCurrentUser(): any {
    return JSON.parse(sessionStorage.getItem('user') || '{}');
  }

  // Get JWT token from sessionStorage
  getToken(): string | null {
    return sessionStorage.getItem('token');
  }

  // Logout method to clear the sessionStorage
  logout(): void {
    sessionStorage.removeItem('user');
    sessionStorage.removeItem('token');
  }
}
