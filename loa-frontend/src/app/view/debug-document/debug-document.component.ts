import {Component, OnInit} from '@angular/core';
import {DebugService} from '../../shared/debug/service/debug.service';
import {DebugDocument} from '../../shared/debug/service/domain/debug-document';

@Component({
  selector: 'app-debug-document',
  templateUrl: './debug-document.component.html',
  styleUrls: ['./debug-document.component.scss']
})
export class DebugDocumentComponent implements OnInit {

  loading = false;
  notFound = false;
  documentId = '';
  document: DebugDocument | undefined;

  constructor(private debugService: DebugService) {
  }

  ngOnInit(): void {
  }

  queryDocument(): void {
    this.document = undefined;
    this.loading = true;
    this.notFound = false;

    this.debugService.queryDocument(this.documentId)
      .subscribe(response => {
          this.loading = false;
          this.document = response;
        },
        error => {
          this.loading = false;
          this.notFound = true;
        });
  }
}
