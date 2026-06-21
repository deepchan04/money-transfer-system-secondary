import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../auth/auth.service';


export interface UserData {
  name: string;
  email: string;
  phoneNumber: string;
  password: string;
}

export interface PaymentRequest {
  payerVpaId: string;
  payeeVpaId: string;
  payerPwd: string;
  idempotencyKey: string;
  amount: number;
  note: string;
  transactionType: string;
}

export interface PaymentResponse {
  id: number;
  amount: number;
  status: 'SUCCESS' | 'FAILED' | 'PENDING';
  transactionType: string;
  note: string;
  payer: any;
  payee: any;
  failureReason: string | null;
  idempotencyKey: string;
  transactionTime: string;
}

@Injectable({
  providedIn: 'root'
})

export class PostService {
  private apiUrl = 'http://localhost:8080/auth/register';
  private analyticsUrl = 'http://localhost:8081/api/snowflake';

  constructor(private http: HttpClient, private authService: AuthService) { }

  // POST method
  createUser(data: UserData): Observable<any> {
    const headers = new HttpHeaders({ 'Content-Type': 'application/json' });

    // http.post(url, body, options)
    // http.post(url, body, options)
    return this.http.post<any>(this.apiUrl, data, { headers });
  }

  // Login method
  loginUser(data: any): Observable<any> {
    const loginUrl = 'http://localhost:8080/auth/login';
    const headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    return this.http.post<any>(loginUrl, data, { headers });
  }

  // Find user by phone number
  findByPhone(phoneNumber: string): Observable<any> {
    const url = `http://localhost:8080/users/findByPhone?phoneNumber=${phoneNumber}`;
    const token = this.authService.getToken();
    if (token) {
      const headers = new HttpHeaders({
        'Authorization': `Bearer ${token}`
      });
      return this.http.get<any>(url, { headers });
    }
    return this.http.get<any>(url);
  }

  // Link bank account
  linkBankAccount(data: any): Observable<any> {
    const url = 'http://localhost:8080/bankaccounts/link';
    const token = this.authService.getToken();
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
    return this.http.post<any>(url, data, { headers });
  }

  // Get bank balance
  getBalance(vpaId: string, password: string): Observable<any> {
    const url = `http://localhost:8080/users/getbalance`;
    const token = this.authService.getToken();
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
    const body = { vpaId, password };
    return this.http.post<any>(url, body, { headers });
  }

  // Get all users
  getUsers(): Observable<any> {
    const url = 'http://localhost:8080/users/getusers';
    const token = this.authService.getToken();
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.get<any>(url, { headers });
  }

  // Get all users for admin
  getAdminUsers(): Observable<any> {
    const url = 'http://localhost:8080/admin/getusers';
    const token = this.authService.getToken();
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.get<any>(url, { headers });
  }

  // Change user status (admin only)
  changeUserStatus(vpaId: string, appStatus: string): Observable<any> {
    const url = 'http://localhost:8080/admin/changeStatus';
    const token = this.authService.getToken();
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
    const body = { vpaId, appStatus };
    return this.http.put<any>(url, body, { headers });
  }

  // Make payment transaction
  makePayment(paymentData: PaymentRequest): Observable<PaymentResponse> {
    const url = 'http://localhost:8080/transactions/pay';
    const token = this.authService.getToken();
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
    return this.http.post<PaymentResponse>(url, paymentData, { headers });
  }

  // Get transaction log
  getTransactionLog(vpaId: string): Observable<any> {
    const url = `http://localhost:8080/transactions/gettranslog?vpaId=${vpaId}`;
    const token = this.authService.getToken();
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.get<any>(url, { headers });
  }

  // Get admin transaction log
  getAdminTransactionLog(): Observable<any> {
    const url = 'http://localhost:8080/admin/gettranslog';
    const token = this.authService.getToken();
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.get<any>(url, { headers });
  }

  // Get user by VPA
  getUserByVpa(vpaId: string): Observable<any> {
    const url = `http://localhost:8080/users/getbyvpa?vpaId=${vpaId}`;
    const token = this.authService.getToken();
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.get<any>(url, { headers });
  }

  // ===== SNOWFLAKE ANALYTICS ENDPOINTS =====
  
  // Get transaction success rate
  getSuccessRate(): Observable<any> {
    const url = `${this.analyticsUrl}/success-rate`;
    return this.http.get<any>(url);
  }

  // Get peak transaction hours
  getPeakHours(): Observable<any> {
    const url = `${this.analyticsUrl}/peak-hours`;
    return this.http.get<any>(url);
  }

  // Get daily transaction volume
  getDailyVolume(): Observable<any> {
    const url = `${this.analyticsUrl}/daily-volume`;
    return this.http.get<any>(url);
  }

  // Get average transaction amount
  getAverageAmount(): Observable<any> {
    const url = `${this.analyticsUrl}/average-amount`;
    return this.http.get<any>(url);
  }

  // Get most active accounts
  getActiveAccounts(): Observable<any> {
    const url = `${this.analyticsUrl}/active-accounts`;
    return this.http.get<any>(url);
  }

  // Get reward points and cards status
  getRewardStatus(): Observable<any> {
    const url = 'http://localhost:8080/rewards/status';
    const token = this.authService.getToken();
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.get<any>(url, { headers });
  }

  // Get user's scratchcards list
  getScratchCards(): Observable<any> {
    const url = 'http://localhost:8080/rewards/list';
    const token = this.authService.getToken();
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.get<any>(url, { headers });
  }

  // Scratch card and reveal coupon details
  scratchCard(cardId: number): Observable<any> {
    const url = `http://localhost:8080/rewards/scratch/${cardId}`;
    const token = this.authService.getToken();
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.post<any>(url, {}, { headers });
  }

  logout(): void {
    // Logout method delegates to AuthService
    this.authService.logout();
  }
}