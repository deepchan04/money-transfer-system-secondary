import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { PostService } from './post.service';

@Injectable({
  providedIn: 'root'
})
export class RewardService {

  private rewardCountSubject = new BehaviorSubject<number>(0);

  rewardCount$ = this.rewardCountSubject.asObservable();

  constructor(private postService: PostService) {}

  refreshRewardCount(): void {
    this.postService.getRewardStatus().subscribe({
      next: (status) => {
        this.rewardCountSubject.next(
          status?.unscratchedCount ?? 0
        );
      },
      error: () => {
        this.rewardCountSubject.next(0);
      }
    });
  }
}