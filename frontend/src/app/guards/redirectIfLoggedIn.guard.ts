import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';

export const redirectIfLoggedIn: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const token = sessionStorage.getItem('token');
  const user = sessionStorage.getItem('user');

  if (token && user) {
    // If user is logged in, redirect to dashboard instead of showing home
    router.navigate(['/dashboard']);
    return false;
  }

  // Not logged in — allow access to the home page
  return true;
};
