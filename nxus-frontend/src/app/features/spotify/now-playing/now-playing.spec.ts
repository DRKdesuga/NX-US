import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NowPlaying } from './now-playing';

describe('NowPlaying', () => {
  let component: NowPlaying;
  let fixture: ComponentFixture<NowPlaying>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NowPlaying]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NowPlaying);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
