import { Component, ViewChild, ChangeDetectorRef, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MessageList, ChatMessage } from '../message-list/message-list';
import { MessageInput } from '../message-input/message-input';
import { ConnectSpotify } from '../../spotify/connect-spotify/connect-spotify';
import { NowPlaying } from '../../spotify/now-playing/now-playing';
import { Nlp, NlpResult } from '../../../core/services/nlp';
import { Gpt } from '../../../core/services/gpt';
import { Spotify, SpotifySearchResponse } from '../../../core/services/spotify';

@Component({
  selector: 'app-chat-shell',
  standalone: true,
  imports: [CommonModule, MessageList, MessageInput, ConnectSpotify, NowPlaying],
  templateUrl: './chat-shell.html',
  styleUrl: './chat-shell.scss'
})
export class ChatShell {
  @ViewChild(MessageList) list?: MessageList;

  messages: ChatMessage[] = [];
  nowPlaying: string | null = null;
  isIntro = true;
  thinking = false;

  constructor(
    private nlp: Nlp,
    private gpt: Gpt,
    private spotify: Spotify,
    private zone: NgZone,
    private cdr: ChangeDetectorRef
  ) {}

  private setThinking(v: boolean) {
    this.zone.run(() => {
      this.thinking = v;
      this.cdr.markForCheck();
    });
  }

  private push(m: Omit<ChatMessage, 'id'>) {
    const id = crypto.randomUUID();
    this.messages = [...this.messages, { id, ...m }];
    queueMicrotask(() => this.list?.scrollToBottom());
  }

  private slot<T = string>(slots: Record<string, unknown> | null | undefined, keys: string[]): T | undefined {
    if (!slots) return undefined as any;
    for (const k of keys) {
      const v = (slots as any)[k];
      if (v !== undefined && v !== null && String(v).trim() !== '') return v as T;
    }
    return undefined;
  }

  async onSend(text: string) {
    if (this.isIntro) this.isIntro = false;

    this.push({ author: 'user', text });
    this.setThinking(true);

    try {
      const nlpRes: NlpResult = await this.nlp.analyze(text);
      const intent = nlpRes?.intent ?? '';

      // --- Play playlist ---
      if (intent === 'play_playlist') {
        const playlist = this.slot<string>(nlpRes.slots as any, ['playlist', 'playlistName', 'list']);
        if (!playlist) { this.push({ author: 'gpt', text: 'Which playlist?' }); return; }
        if (!this.spotify.isConnected()) { this.push({ author: 'gpt', text: 'Please connect Spotify first.' }); return; }

        const resp = await this.spotify.playPlaylist(playlist);
        const ok = (resp as any)?.ok ?? (resp as any)?.success ?? true;
        if (ok) {
          this.nowPlaying = playlist;
          this.push({ author: 'gpt', text: `Playing playlist: ${playlist}`, meta: { playing: playlist } });
        } else {
          this.push({ author: 'gpt', text: 'Playlist not found or failed to play.' });
        }
        return;
      }

      // --- Play music (track search -> play) ---
      if (intent === 'play_music') {
        const title  = this.slot<string>(nlpRes.slots as any, ['title', 'song', 'songTitle', 'track']) ?? '';
        const artist = this.slot<string>(nlpRes.slots as any, ['artist', 'artistName', 'singer']) ?? '';
        if (!this.spotify.isConnected()) { this.push({ author: 'gpt', text: 'Please connect Spotify first.' }); return; }

        const query = `${title} ${artist}`.trim() || undefined;
        const res: SpotifySearchResponse = await this.spotify.search({
          title: title || undefined,
          artist: artist || undefined,
          query
        });

        const uri = res?.uri;
        if (!uri) { this.push({ author: 'gpt', text: 'No result found.' }); return; }

        const playResp = await this.spotify.play(uri);
        const ok = (playResp as any)?.ok ?? (playResp as any)?.success ?? true;

        const label = res?.name && res?.artist ? `${res.name} – ${res.artist}` : (title || 'Track');
        if (ok) {
          this.nowPlaying = label;
          this.push({ author: 'gpt', text: `Playing: ${label}`, meta: { playing: label } });
        } else {
          this.push({ author: 'gpt', text: 'Failed to start playback. Please open a Spotify client then try again.' });
        }
        return;
      }

      // --- Transport controls ---
      if (intent === 'pause_music') {
        if (!this.spotify.isConnected()) { this.push({ author: 'gpt', text: 'Please connect Spotify first.' }); return; }
        await this.spotify.pause();
        this.push({ author: 'gpt', text: 'Paused.' });
        return;
      }

      if (intent === 'next_track') {
        if (!this.spotify.isConnected()) { this.push({ author: 'gpt', text: 'Please connect Spotify first.' }); return; }
        await this.spotify.next();
        this.push({ author: 'gpt', text: 'Next track.' });
        return;
      }

      if (intent === 'previous_track') {
        if (!this.spotify.isConnected()) { this.push({ author: 'gpt', text: 'Please connect Spotify first.' }); return; }
        await this.spotify.previous();
        this.push({ author: 'gpt', text: 'Previous track.' });
        return;
      }

      // --- Chat/GPT ---
      if (intent === 'chat_gpt_query' || !intent) {
        const r = await this.gpt.ask(text);
        this.push({ author: 'gpt', text: r?.result ?? '' });
        return;
      }

      // Fallback inconnu (rare, le /nlp renverra plutôt 400)
      this.push({ author: 'gpt', text: "I didn't get that, try again." });

    } catch (err: any) {
      // Affiche le message d’aide si /nlp a renvoyé 400
      this.push({ author: 'gpt', text: err?.message || 'Unknown error.' });
    } finally {
      this.setThinking(false);
    }
  }
}
