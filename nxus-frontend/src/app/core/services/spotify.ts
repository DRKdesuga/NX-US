import { Injectable, signal } from '@angular/core';
import { BaseFetch } from './base-fetch';
import { API } from '../../config/app-config';

export interface SpotifySearchResponse {
  name: string;
  artist: string;
  url: string;
  uri: string;
}

export interface SpotifyPlayResponse {
  ok?: boolean;
  success?: boolean;
  message?: string;
}

@Injectable({ providedIn: 'root' })
export class Spotify {
  private readonly tokenKey = 'spotify_token';
  accessToken = signal<string | null>(localStorage.getItem(this.tokenKey));

  constructor(private http: BaseFetch) {
    this.consumeTokenFromHash();

    window.addEventListener('message', (ev: MessageEvent) => {
      const data = ev?.data as any;
      if (data && data.type === 'spotify_token' && typeof data.access_token === 'string') {
        this.setToken(data.access_token);
      }
    });
  }

  private consumeTokenFromHash() {
    const m = (window.location.hash || '').match(/spotify_token=([^&]+)/);
    if (m) {
      const tok = decodeURIComponent(m[1]);
      this.setToken(tok);
      history.replaceState(null, '', window.location.pathname + window.location.search);
    }
  }

  isConnected() { return !!this.accessToken(); }

  setToken(t: string) {
    this.accessToken.set(t);
    localStorage.setItem(this.tokenKey, t);
  }

  clearToken() {
    this.accessToken.set(null);
    localStorage.removeItem(this.tokenKey);
  }

  private headers(): Record<string, string> {
    const t = this.accessToken();
    return t ? { Authorization: `Bearer ${t}` } : {};
  }

  search(body: { title?: string; artist?: string; query?: string }) {
    return this.http.post<SpotifySearchResponse>(API.spotify.search(), body, this.headers());
  }

  play(uri: string) {
    return this.http.post<SpotifyPlayResponse>(API.spotify.play(), { uri }, this.headers());
  }

  playPlaylist(name: string) {
    return this.http.post<SpotifyPlayResponse>(API.spotify.playPlaylist(), { name }, this.headers());
  }

  pause()   { return this.http.post<SpotifyPlayResponse>(API.spotify.pause(),   {}, this.headers()); }
  next()    { return this.http.post<SpotifyPlayResponse>(API.spotify.next(),    {}, this.headers()); }
  previous(){ return this.http.post<SpotifyPlayResponse>(API.spotify.previous(),{}, this.headers()); }

  // FIX: always Promise<void>. Never return the value of the assignment (string).
  async loginRedirect(): Promise<void> {
    try {
      const { url } = await this.http.get<{ url: string }>(API.spotify.login());
      const w = window.open(url, 'spotify_oauth', 'width=520,height=700,noopener');
      if (!w) {
        // same-tab fallback; do not return the string value of the assignment
        window.location.href = url;
      }
    } catch {
      // last-resort fallback; again, do not return the assignment value
      window.location.href = API.spotify.login();
    }
  }
}
