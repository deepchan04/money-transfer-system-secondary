import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {ServerStatusService} from "./service/server-status.service";
import { Observable } from 'rxjs/internal/Observable';
import { CommonModule } from '@angular/common';
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule],
  templateUrl: './app.component.html'
})
export class AppComponent {
 title = 'MTS';
}
