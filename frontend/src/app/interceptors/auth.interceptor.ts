import { inject } from '@angular/core';
import { HttpInterceptorFn } from '@angular/common/http';
import { throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';

function isTokenExpired(token: string): boolean {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.exp * 1000 <= Date.now();
  } catch {
    return true;
  }
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {

  const authService = inject(AuthService);
  const snackBar = inject(MatSnackBar);
  const token = authService.getToken();

  if (token && isTokenExpired(token)) {
        snackBar.open(
          'Session expired! Please login again!',
          'Close',
          {
            duration: 5000,
            horizontalPosition: 'center',
            verticalPosition: 'top',
          }
        );    authService.logout();
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