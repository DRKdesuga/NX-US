import { TestBed } from '@angular/core/testing';

import { Gpt } from './gpt';

describe('Gpt', () => {
  let service: Gpt;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Gpt);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
