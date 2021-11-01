import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {Subject} from 'rxjs';
import {debounceTime, distinctUntilChanged} from 'rxjs/operators';
import {HttpClient} from '@angular/common/http';
import {SearchService} from '../../shared/search/service/search.service';
import {DomSanitizer, SafeResourceUrl} from '@angular/platform-browser';
import {SearchHit} from '../../shared/search/service/domain/search-hit';

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.scss']
})
export class SearchComponent implements OnInit {

  statistics: any;
  searchText = '';
  modelChanged: Subject<string> = new Subject<string>();
  documentLengths: string[][];
  documentLength: any = undefined;
  languages: string[][];
  language: any = undefined;
  resultSize = 10;
  fileTypes: any = {
    PDF: false,
    DOC: false,
    PPT: false,
    RTF: false,
    XLS: false,
    DOCX: false,
    XLSX: false,
    EPUB: false,
    MOBI: false
  };
  hits: SearchHit[] = [];
  hitCount = 0;
  totalPages = 0;
  page = 0;
  loading = false;
  openPdfs: Map<string, boolean> = new Map();
  loadedImages: Map<string, boolean> = new Map();

  constructor(private route: ActivatedRoute, private http: HttpClient, private searchService: SearchService,
              private sanitizer: DomSanitizer) {
    this.modelChanged.pipe(debounceTime(300), distinctUntilChanged())
      .subscribe(searchText => {
        this.searchText = searchText;
        this.page = 0;

        this.refreshHits();
      });

    this.languages = [
      ['af', 'afrikaans'],
      ['sq', 'albanian'],
      ['ar', 'arabic'],
      ['hy', 'armenian'],
      ['az', 'azerbaijani'],
      ['eu', 'basque'],
      ['be', 'belarusian'],
      ['bn', 'bengali'],
      ['nb', 'bokmal'],
      ['bs', 'bosnian'],
      ['bg', 'bulgarian'],
      ['ca', 'catalan'],
      ['zh', 'chinese'],
      ['hr', 'croatian'],
      ['cs', 'czech'],
      ['da', 'danish'],
      ['nl', 'dutch'],
      ['en', 'english'],
      ['eo', 'esperanto'],
      ['et', 'estonian'],
      ['fi', 'finnish'],
      ['fr', 'french'],
      ['lg', 'ganda'],
      ['ka', 'georgian'],
      ['de', 'german'],
      ['el', 'greek'],
      ['gu', 'gujarati'],
      ['he', 'hebrew'],
      ['hi', 'hindi'],
      ['hu', 'hungarian'],
      ['is', 'icelandic'],
      ['id', 'indonesian'],
      ['ga', 'irish'],
      ['it', 'italian'],
      ['ja', 'japanese'],
      ['kk', 'kazakh'],
      ['ko', 'korean'],
      ['la', 'latin'],
      ['lv', 'latvian'],
      ['lt', 'lithuanian'],
      ['mk', 'macedonian'],
      ['ms', 'malay'],
      ['mr', 'marathi'],
      ['mn', 'mongolian'],
      ['nn', 'nynorsk'],
      ['fa', 'persian'],
      ['pl', 'polish'],
      ['pt', 'portuguese'],
      ['pa', 'punjabi'],
      ['ro', 'romanian'],
      ['ru', 'russian'],
      ['sr', 'serbian'],
      ['sn', 'shona'],
      ['sk', 'slovak'],
      ['sl', 'slovene'],
      ['so', 'somali'],
      ['st', 'sotho'],
      ['es', 'spanish'],
      ['sw', 'swahili'],
      ['sv', 'swedish'],
      ['tl', 'tagalog'],
      ['ta', 'tamil'],
      ['te', 'telugu'],
      ['th', 'thai'],
      ['ts', 'tsonga'],
      ['tn', 'tswana'],
      ['tr', 'turkish'],
      ['uk', 'ukrainian'],
      ['ur', 'urdu'],
      ['vi', 'vietnamese'],
      ['cy', 'welsh'],
      ['xh', 'xhosa'],
      ['yo', 'yoruba'],
      ['zu', 'zulu']
    ];

    this.documentLengths = [
      ['SHORT_STORY', 'Short story (1 - 10 pages)'],
      ['NOVELETTE', 'Novelette (11 - 50 pages)'],
      ['NOVELLA', 'Novella (51 - 150 pages)'],
      ['NOVEL', 'Novel (150+ pages)']
    ];
  }

  ngOnInit(): void {
    this.statistics = this.route.snapshot.data.statistics;
  }

  changed(text: string): void {
    this.modelChanged.next(text);
  }

  setLanguage(language: any): void {
    document.getElementById('language-dropdown')
      ?.classList.remove('show');

    this.language = language;

    this.refreshHits();
  }

  setResultSize(resultSize: number): void {
    this.resultSize = resultSize;
    this.page = 0;

    this.refreshHits();
  }

  setDocumentLength(documentLength: any): void {
    document.getElementById('length-dropdown')
      ?.classList.remove('show');

    this.documentLength = documentLength;

    this.refreshHits();
  }

  jumpToPage(page: number): void {
    this.page = page;

    this.refreshHits();
  }

  isImageLoaded(documentId: string): boolean | undefined {
    console.log('Get the preview loaded for document with id: ' + documentId + '.');

    if (!this.loadedImages.has(documentId)) {
      return false;
    }

    return this.loadedImages.get(documentId);
  }

  setImageLoaded(documentId: string, event: any): void {
    console.log('Set the preview loaded for document with id: ' + documentId + '.');

    event.target.style.display = 'block';

    this.loadedImages.set(documentId, true);
  }

  openPdf(pdfId: string): void {
    if (!this.openPdfs.has(pdfId)) {
      this.openPdfs.set(pdfId, false);
    }

    this.openPdfs.set(pdfId, !this.openPdfs.get(pdfId));
  }

  getPageCountToDisplayOnLeftSide(): number[] {
    if (this.page - 5 > 0) {
      return [this.page - 5, this.page - 4, this.page - 3, this.page - 2, this.page - 1];
    } else {
      const result = [];
      for (let i = 0; i < this.page; i++) {
        result.push(this.page - i - 1);
      }
      result.reverse();

      return result;
    }
  }

  getPageCountToDisplayOnRightSide(): number[] {
    if (this.totalPages > this.page + 5) {
      return [this.page + 1, this.page + 2, this.page + 3, this.page + 4, this.page + 5];
    } else {
      const result = [];
      for (let i = 0; i < this.totalPages - this.page - 1; i++) {
        result.push(this.page + i + 1);
      }

      return result;
    }
  }

  getDocumentUrl(documentId: string): SafeResourceUrl {
    return this.sanitizer.bypassSecurityTrustResourceUrl('./document/' + documentId);
  }

  getLanguageName(languageId: string): string {
    for (const language of this.languages) {
      if (language[0] === languageId) {
        return language[1];
      }
    }

    return 'unknown';
  }

  refreshHits(): void {
    console.log('Should refresh hits here!', this.fileTypes);

    if (this.searchText === '') {
      this.hits = [];
      this.hitCount = 0;
      this.totalPages = 0;

      return;
    }

    this.loading = true;
    this.hits = [];
    this.hitCount = 0;
    this.totalPages = 0;
    this.openPdfs = new Map<string, boolean>();
    this.loadedImages = new Map<string, boolean>();

    const exactMatch = this.searchText.startsWith('"') && this.searchText.endsWith('"');

    this.searchService.searchDocuments(this.searchText, this.page, this.language, exactMatch,
      this.documentLength, this.resultSize, this.fileTypes)
      .subscribe(response => {
        this.hits = response.searchHits;
        this.hitCount = response.totalHitCount;
        this.totalPages = Math.ceil(response.totalHitCount / this.resultSize);
        this.loading = false;
      });
  }
}
