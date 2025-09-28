import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Spotify } from '../../../core/services/spotify';

@Component({
  selector: 'app-connect-spotify',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './connect-spotify.html',
  styleUrl: './connect-spotify.scss'
})
export class ConnectSpotify {
  constructor(public spotify: Spotify) {}
  connect() { this.spotify.loginRedirect(); }
}
