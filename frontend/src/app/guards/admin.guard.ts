import { inject } from '@angular/core';
import { AuthService } from '../auth/auth.service';
import { Router, CanActivateFn } from '@angular/router';

export const adminGuard: CanActivateFn = (route, state) => {
    const router = inject(Router);
    const token = sessionStorage.getItem('token');
    const authService = inject(AuthService);
  const user = authService.getCurrentUser();
    let userData;
    if(user){
        userData = user;        
    }

    if (userData.role === "ROLE_ADMIN") {
        return true;
    } else {
        router.navigate(['/auth']);
        return false;
    }
};
