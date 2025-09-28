import { Injectable } from '@angular/core';
import { BaseFetch } from './base-fetch';
import { API } from '../../config/app-config';

export interface GptResponse { result: string }

@Injectable({ providedIn: 'root' })
export class Gpt {
  constructor(private http: BaseFetch) {}
  ask(prompt: string): Promise<GptResponse> {
    return this.http.post<GptResponse>(API.gptAsk(), { prompt });
  }
}
