import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { Router, CanActivateFn } from '@angular/router';

export const authGuard: CanActivateFn = (route, state) => {
    const router = inject(Router);
    const token = sessionStorage.getItem('token');
    const authService = inject(AuthService);
    const user = authService.getCurrentUser();
        let userData;
        if(user){
            userData = user;        
        }

        if (userData?.role === "ROLE_ADMIN" || token && userData?.appStatus !== "CLOSED") {
            return true;
        } else {
            router.navigate(['/']);
            return false;
        }
    };
