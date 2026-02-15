import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { LoginComponent } from './login/login.component';
import { SignupComponent } from './signup/signup.component';
import { Router } from '@angular/router';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    LoginComponent,
    SignupComponent,
],
  templateUrl: './auth.component.html',
  styleUrls: ['./auth.component.scss']
})
export class AuthComponent {
  mode: 'login' | 'signup' = 'login';

  constructor(private router: Router){}

  switchTo(mode: 'login' | 'signup') {
    this.mode = mode;
  }

   goHome() {
    this.router.navigate(['/']); // Navigates to the home page (usually the root route)
  }
}
