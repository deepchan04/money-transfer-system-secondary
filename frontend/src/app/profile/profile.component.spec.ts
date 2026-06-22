import { TestBed } from '@angular/core/testing';
import { ProfileComponent } from './profile.component';
import { AuthService } from '../auth/auth.service';
import { of } from 'rxjs';

describe('ProfileComponent', () => {
  const mockUser = {
    name: 'John Doe',
    appStatus: 'ACTIVE',
    bankAccount: {
      accountNumber: 'ACC123456'
    }
  };

  const authServiceMock = {
    currentUser$: of(mockUser)
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProfileComponent],
      providers: [
        { provide: AuthService, useValue: authServiceMock }
      ]
    }).compileComponents();
  });

  it('should create component', () => {
    const fixture = TestBed.createComponent(ProfileComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should load user from AuthService observable', () => {
    const fixture = TestBed.createComponent(ProfileComponent);
    const component = fixture.componentInstance;

    expect(component.user.name).toBe('John Doe');
  });

  it('should return account number', () => {
    const fixture = TestBed.createComponent(ProfileComponent);
    const component = fixture.componentInstance;

    expect(component.getAccountNumber()).toBe('ACC123456');
  });

  it('should return ACTIVE status color', () => {
    const fixture = TestBed.createComponent(ProfileComponent);
    const component = fixture.componentInstance;

    expect(component.getStatusColor()).toBe('active');
  });

  it('should return ACTIVE status text', () => {
    const fixture = TestBed.createComponent(ProfileComponent);
    const component = fixture.componentInstance;

    expect(component.getStatusText()).toBe('Active');
  });

  it('should return initials', () => {
    const fixture = TestBed.createComponent(ProfileComponent);
    const component = fixture.componentInstance;

    expect(component.getInitials()).toBe('JD');
  });
});