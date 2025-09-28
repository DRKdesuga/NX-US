import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-now-playing',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './now-playing.html',
  styleUrl: './now-playing.scss'
})
export class NowPlaying {
  @Input() title: string | null = null;
}
