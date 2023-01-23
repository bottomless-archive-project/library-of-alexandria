import {Component, OnInit} from '@angular/core';
import {DebugService} from '../../shared/debug/service/debug.service';
import {DebugLocation} from '../../shared/debug/service/domain/debug-location';

@Component({
  selector: 'app-debug-location',
  templateUrl: './debug-location.component.html',
  styleUrls: ['./debug-location.component.scss']
})
export class DebugLocationComponent implements OnInit {

  loading = false;
  notFound = false;
  locationId = '';
  location: DebugLocation | undefined;

  constructor(private debugService: DebugService) {
  }

  ngOnInit(): void {
  }

  queryLocation(): void {
    this.location = undefined;
    this.loading = true;
    this.notFound = false;

    this.debugService.queryLocation(this.locationId)
      .subscribe(response => {
          this.loading = false;
          this.location = response;
        },
        error => {
          this.loading = false;
          this.notFound = true;
        });
  }
}
