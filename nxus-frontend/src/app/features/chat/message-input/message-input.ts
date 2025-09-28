import { Component, ElementRef, EventEmitter, Output, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-message-input',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './message-input.html',
  styleUrl: './message-input.scss'
})
export class MessageInput {
  @Output() send = new EventEmitter<string>();
  @ViewChild('box') box?: ElementRef<HTMLInputElement>;

  submit() {
    const value = (this.box?.nativeElement.value || '').trim();
    if (!value) return;
    this.send.emit(value);
    if (this.box) this.box.nativeElement.value = '';
  }
}
