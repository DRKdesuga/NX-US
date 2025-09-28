import { Component, Input, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import {TypingIndicator} from '../../../typing-indicator/typing-indicator';

export type Author = 'user' | 'gpt';
export interface ChatMessage {
  id: string;
  author: Author;
  text: string;
  meta?: { playing?: string };
}

@Component({
  selector: 'app-message-list',
  standalone: true,
  imports: [CommonModule, TypingIndicator],
  templateUrl: './message-list.html',
  styleUrl: './message-list.scss'
})
export class MessageList {
  @Input() messages: ChatMessage[] = [];
  @Input() thinking = false;

  @ViewChild('container') container?: ElementRef<HTMLElement>;

  scrollToBottom() {
    const el = this.container?.nativeElement;
    if (!el) return;
    el.scrollTop = el.scrollHeight;
  }
}
