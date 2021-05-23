import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {

  statistics: any;

  constructor(private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.statistics = this.route.snapshot.data.statistics;
  }

  keepOrder(a: any, b: any): any {
    return a;
  }
}
