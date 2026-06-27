import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Artists } from './artists';

describe('Artists', () => {
  let component: Artists;
  let fixture: ComponentFixture<Artists>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Artists],
    }).compileComponents();

    fixture = TestBed.createComponent(Artists);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
