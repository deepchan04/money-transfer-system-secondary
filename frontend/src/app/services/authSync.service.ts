import { Injectable, Injector,  inject } from '@angular/core';
import { Subject } from 'rxjs'; // Note: Cleaned up the internal/Subject import path
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthSyncService {
  private channel = new BroadcastChannel('auth-channel');

  // 1. CREATE A UNIQUE IDENTITY FOR THIS TAB INSTANCE
  private readonly tabId = crypto.randomUUID(); 

  private tokenReceivedSubject = new Subject<string>();
  tokenReceived$ = this.tokenReceivedSubject.asObservable();

  private loginSubject = new Subject<string>();
  login$ = this.loginSubject.asObservable();

  private logoutSubject = new Subject<void>();
  logout$ = this.logoutSubject.asObservable();

  constructor(private injector: Injector) {
    this.channel.onmessage = (event) => {
      
      if (event.data.senderTabId === this.tabId) {
        return;
      }

      switch (event.data.type) {
        case 'REQUEST_TOKEN':
          const token = sessionStorage.getItem('token');
          if (token) {
            this.channel.postMessage({
              type: 'TOKEN_RESPONSE',
              token,
              senderTabId: this.tabId // Always include who sent it
            });
          }
          break;

        case 'TOKEN_RESPONSE':
          this.tokenReceivedSubject.next(event.data.token);
          break;
        
        case 'LOGIN':
          if (!sessionStorage.getItem('token')) {
            sessionStorage.setItem('token', event.data.token);
            this.loginSubject.next(event.data.token);
          }
          break; 

        // 3. HANDLE THE SELECTIVE LOGOUT COMMAND
        case 'SELECTIVE_LOGOUT':
          // 1. Get the current logged-in user's ID from this tab's session/token
          const authService = this.injector.get(AuthService);
          const currentUser = authService.getCurrentUser(); 

          // Verify if the active user matches the targeted logout ID
          if (currentUser && currentUser.id === event.data.targetUserId) {
            sessionStorage.clear();
            this.logoutSubject.next();
          } else {
            
          }
          break;

        case 'LOGOUT':
          sessionStorage.clear();
          this.logoutSubject.next();
          break;
      }
    };
  }

  requestToken() {
    this.channel.postMessage({
      type: 'REQUEST_TOKEN',
      senderTabId: this.tabId
    });
  }
  
  broadcastLogin(token: string) {
    this.channel.postMessage({
      type: 'LOGIN',
      token,
      senderTabId: this.tabId
    });
  }

  // 4. IMPLEMENT THE SELECTIVE LOGOUT BROADCAST
  broadcastSelectiveLogout(userId: string) {
    this.channel.postMessage({
      type: 'SELECTIVE_LOGOUT',
      senderTabId: this.tabId,
      targetUserId: userId 
    });
  }

  broadcastLogout() {
    this.channel.postMessage({
      type: 'LOGOUT',
      senderTabId: this.tabId
    });
  }
}