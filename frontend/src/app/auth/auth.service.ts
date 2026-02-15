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

  // Save user and JWT in localStorage
  saveUserAndToken(user: any, token: string): void {
    localStorage.setItem('user', JSON.stringify(user));
    localStorage.setItem('token', token);
  }

  // Get current user from localStorage
  getCurrentUser(): any {
    return JSON.parse(localStorage.getItem('user') || '{}');
  }

  // Get JWT token from localStorage
  getToken(): string | null {
    return localStorage.getItem('token');
  }

  // Logout method to clear the localStorage
  logout(): void {
    localStorage.removeItem('user');
    localStorage.removeItem('token');
  }
}
