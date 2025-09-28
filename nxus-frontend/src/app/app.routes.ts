import { Routes } from '@angular/router';
import { ChatShell } from './features/chat/chat-shell/chat-shell';

export const routes: Routes = [
  { path: '', component: ChatShell },
  { path: 'auth/spotify', component: ChatShell },
  { path: '**', redirectTo: '' }
];
