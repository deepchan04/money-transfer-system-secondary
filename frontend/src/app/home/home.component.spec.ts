import { TestBed } from '@angular/core/testing';
import { HomeComponent } from './home.component';
import { RouterTestingModule } from '@angular/router/testing';

describe('HomeComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HomeComponent,RouterTestingModule], // standalone component
    }).compileComponents();
  });

  it('should create component', () => {
    const fixture = TestBed.createComponent(HomeComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should render hero section', () => {
    const fixture = TestBed.createComponent(HomeComponent);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.querySelector('.hero')).toBeTruthy();
    expect(compiled.textContent).toContain('Fast, Secure Money Transfers');
  });

  it('should render feature cards', () => {
    const fixture = TestBed.createComponent(HomeComponent);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;

    const cards = compiled.querySelectorAll('mat-card');
    expect(cards.length).toBeGreaterThanOrEqual(3);
  });

  it('should render how it works section', () => {
    const fixture = TestBed.createComponent(HomeComponent);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.textContent).toContain('How It Works');
    expect(compiled.querySelector('.how-it-works')).toBeTruthy();
  });

  it('should render CTA section', () => {
    const fixture = TestBed.createComponent(HomeComponent);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.textContent).toContain('Ready to Get Started');
    expect(compiled.querySelector('.cta')).toBeTruthy();
  });

  it('should contain router links to auth', () => {
    const fixture = TestBed.createComponent(HomeComponent);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;

    const buttons = compiled.querySelectorAll('button[routerLink]');
    expect(buttons.length).toBeGreaterThan(0);
  });

  it('should render footer', () => {
    const fixture = TestBed.createComponent(HomeComponent);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.querySelector('footer')).toBeTruthy();
    expect(compiled.textContent).toContain('MTS Payment App');
  });
});