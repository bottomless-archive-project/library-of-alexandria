import {Component, OnInit} from '@angular/core';

@Component({
  selector: 'app-debug-document',
  templateUrl: './debug-document.component.html',
  styleUrls: ['./debug-document.component.scss']
})
export class DebugDocumentComponent implements OnInit {

  documentId = '';

  constructor() {
  }

  ngOnInit(): void {
  }

  queryDocument(): void {
    //TODO!
  }
}
