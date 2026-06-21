import { inject } from '@angular/core';
import { HttpInterceptorFn } from '@angular/common/http';
import { throwError } from 'rxjs';
import { AuthService } from '../auth/auth.service';

function isTokenExpired(token: string): boolean {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.exp * 1000 <= Date.now();
  } catch {
    return true;
  }
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  console.log('Interceptor:', req.method, req.url);

  const authService = inject(AuthService);
  const token = authService.getToken();

  // Logout only if JWT has expired
  if (token && isTokenExpired(token)) {
    alert('Your session has expired. Please log in again.');
    authService.logout();
    return throwError(() => new Error('Token expired'));
  }

  const authReq = token
    ? req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      })
    : req;

  return next(authReq);
};