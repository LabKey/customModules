/*
 * Copyright (c) 2016 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
Ext4.define('AtlasTools.NAb.RecoverFolders', {
    extend: 'Ext.panel.Panel',
    border: false,

    initComponent : function()
    {
        this.items = [
            this.getRunToolButton(),
            this.getSearchProgressBar(),
            this.getMessagePanel(),
            this.getFilesFoundHeaderView(),
            this.getFilesFoundBodyView(),
            this.getFilesNotFoundHeaderView(),
            this.getFilesNotFoundBodyView(),
            this.getFixPathsButton(),
            this.getConvertStoreToExcelButton()
        ];

        this.callParent();
    },

    getFilesFoundHeaderView: function()
    {
        if (!this.filesFoundHeaderView)
        {
            this.filesFoundHeaderView = this.makeFileHeaderView("Completely Fixable Folders (All File Paths Were Found)");
        }
        return this.filesFoundHeaderView;
    },

    getFilesFoundBodyView: function()
    {
        if (!this.filesFoundBodyView)
        {
            this.filesFoundBodyView = this.makeFileBodyView(this.getFilesFoundFoldersStore());
        }
        return this.filesFoundBodyView;
    },

    getFilesNotFoundHeaderView: function()
    {
        if (!this.filesNotFoundHeaderView)
        {
            this.filesNotFoundHeaderView = this.makeFileHeaderView("Not Completely Fixable Folders (Some/All File Paths Were Not Found)");
        }
        return this.filesNotFoundHeaderView;
    },

    getFilesNotFoundBodyView: function()
    {
        if (!this.filesNotFoundBodyView)
        {
            this.filesNotFoundBodyView = this.makeFileBodyView(this.getFilesNotFoundFoldersStore());
        }
        return this.filesNotFoundBodyView;
    },

    getFilesFoundStore: function()
    {
        if (!this.filesFoundStore)
        {
            this.filesFoundStore = this.makeFileStore();
        }

        return this.filesFoundStore;
    },

    getFilesNotFoundStore: function()
    {
        if (!this.filesNotFoundStore)
        {
            this.filesNotFoundStore = this.makeFileStore();
        }

        return this.filesNotFoundStore;
    },

    getFilesFoundFoldersStore: function()
    {
        if (!this.filesFoundFoldersStore)
        {
            this.filesFoundFoldersStore = this.makeFolderStore();
        }

        return this.filesFoundFoldersStore;
    },

    getFilesNotFoundFoldersStore: function()
    {
        if (!this.filesNotFoundFoldersStore)
        {
            this.filesNotFoundFoldersStore = this.makeFolderStore();
        }

        return this.filesNotFoundFoldersStore;
    },

    makeFileStore : function()
    {
        return Ext4.create('Ext.data.Store', {
            fields: [
                {name: 'RowId'},
                {name: 'Name'},
                {name: 'DataFileUrl'},
                {name: 'FileExists'},
                {name: 'FileExistsAtCurrent', defaultValue: null},
                {name: 'FilePathFixed', defaultValue: null},
                {name: 'NewDataFileUrl', defaultValue: null}
            ],
            data: []
        });
    },

    makeFolderStore : function()
    {
        return Ext4.create('Ext.data.Store', {
            fields: [
                {name: 'ContainerPath'},
                {name: 'ContainerHref'},
                {name: 'UnprocessedFileCount'},
                {name: 'FixableFileCount'},
                {name: 'UnfixableFileCount'},
                {name: 'FixedFileCount'},
                {name: 'FailedFileCount'}
            ],
            data: []
        });
    },

    getRunToolButton : function()
    {
        if (!this.runToolBtn)
        {
            var isRecoveryPage = LABKEY.ActionURL.getController() + '-' + LABKEY.ActionURL.getAction() == 'atlastools-recoverNAbFolders';

            if (!isRecoveryPage)
            {
                this.runToolBtn = Ext4.create('Ext.button.Button', {
                    text: 'Go To Search Page',
                    style: 'margin-bottom: 10px;',
                    scope: this,
                    handler: function()
                    {
                        // since the search results can display as a long vertical page of details, go to the action in its own page
                        window.location = LABKEY.ActionURL.buildURL('atlastools', 'recoverNAbFolders');
                    }
                });
            }
            else  // must be actual search
            {
                this.runToolBtn = Ext4.create('Ext.button.Button', {
                    text: 'Run Search',
                    style: 'margin-bottom: 10px;',
                    scope: this,
                    handler: function()
                    {
                        this.getSearchProgressBar().updateText('Loading run data folder count...');
                        this.getSearchProgressBar().show();

                        // get all children of current container and go through them in order (by chaining calls to queryForOrphanedFiles())
                        LABKEY.Security.getContainers({
                            scope: this,
                            success: function(containersInfo)
                            {
                                this.childContainers = containersInfo.children;
                                this.childContainers.push(LABKEY.Security.currentContainer);  // also process current container
                                // NOTE: currentContainer has less info than containers from getContainers(), but we only use path so it's not a problem
                                this.containersTotal = this.childContainers.length;
                                this.currentContainerIndex = 0;
                                this.haveFilesBeenFound = false;
                                this.resetSearchData();
                                this.getRunToolButton().disable();
                                this.getFixPathsButton().disable();
                                this.getConvertStoreToExcelButton().disable();
                                this.getConvertStoreToExcelButton().hide();
                                this.updateProgressBar(this.currentContainerIndex, this.containersTotal, true);
                                this.processNextSearchContainer();
                            }
                        });
                    }
                });
            }
        }

        return this.runToolBtn;
    },

    getFixPathsButton : function()
    {
        if (!this.fixPathsBtn)
        {
            this.fixPathsBtn = Ext4.create('Ext.button.Button', {
                text: 'Fix Paths',
                style: 'margin-top: 20px;',
                hidden: true,
                scope: this,
                handler: function()
                {
                    this.getSearchProgressBar().updateText('Loading run data folder count...');
                    this.getSearchProgressBar().show();
                    this.getRunToolButton().disable();
                    this.getFixPathsButton().disable();
                    this.getConvertStoreToExcelButton().disable();
                    this.getConvertStoreToExcelButton().hide();
                    this.updateProgressBar(0, this.getFilesFoundStore().getCount(), false);
                    this.attemptFixDataFilePaths();
                }
            });
        }

        return this.fixPathsBtn;
    },

    getConvertStoreToExcelButton : function()
    {
        if (!this.convertStoreToExcelBtn)
        {
            this.convertStoreToExcelBtn = Ext4.create('Ext.button.Button', {
                text: 'Export Fixes To Excel',
                style: 'margin-top: 20px; margin-left: 10px',
                hidden: true,
                scope: this,
                handler: function()
                {
                    this.convertStoreToExcel();
                }
            });
        }

        return this.convertStoreToExcelBtn;
    },

    getSearchProgressBar : function()
    {
        if (!this.searchProgressBar)
        {
            this.searchProgressBar = Ext4.create('Ext.ProgressBar', {
                hidden: true,
                style: 'margin: 10px 0;',
                width: 500
            });
        }

        return this.searchProgressBar;
    },

    getMessagePanel : function()
    {
        if (!this.messagePanel)
        {
            this.messagePanel = Ext4.create('Ext.Component', {
                hidden: true
            });
        }

        return this.messagePanel;
    },

    // Header and body are split so that one can use a data array and the other can use a store object.
    // This is because mixing them in a single view is bad and leads to a trip to Unexpected Behavior Land
    makeFileHeaderView : function(heading)
    {
        return Ext4.create('Ext.view.View', {
            data: {
                heading: heading,
                sectionTotalUpdated: null,
                sectionTotalSkipped: null,
                showHeading: false
            },
            tpl: new Ext4.XTemplate(
                '<div class="section" id="nab-section-heading">',
                    '<tpl if="sectionTotalUpdated != null">',
                    '<div class="section-total fixed">Total Files Fixed: {sectionTotalUpdated}</div>',
                    '</tpl>',
                    '<tpl if="sectionTotalSkipped != null">',
                    '<div class="section-total not-fixed">Total File Fixes Failed: {sectionTotalSkipped}</div>',
                    '</tpl>',
                    '<tpl if="showHeading === true">',
                        '<span class="section-heading">{heading}</span>',
                    '</tpl>',
                '</div>'
            ), getHeading: function() { return heading; }
        });
    },

    makeFileBodyView : function(store)
    {
        return Ext4.create('Ext.view.View', {
            store: store,
            tpl: new Ext4.XTemplate(
                '<div class="section-body" id="nab-section-body">',
                    '<tpl for=".">',
                        '<div class="field-header">Container Path: <a href="{ContainerHref}" target="_blank">{ContainerPath}</a></div>',
                        '<div class="field-content">Number Of Run Files: {UnprocessedFileCount}</div>',
                        '<div class="field-content">Number Of Fixable Run Files: {FixableFileCount}</div>',
                        '<div class="field-content">Number Of Unfixable Run Files: {UnfixableFileCount}</div>',
                        '<tpl if="FixedFileCount !== \'\'">',
                            '<div class="field-content fixed">Number Of Fixed Run Files: {FixedFileCount}</div>',
                        '</tpl>',
                        '<tpl if="FailedFileCount !== \'\'">',
                            '<div class="field-content not-fixed">Number Of Failed Fixes Of Run Files: {FailedFileCount}</div>',
                        '</tpl>',
                    '</tpl>',
                '</div>'
            )
        });
    },

    resetSearchData : function()
    {
        this.getFilesFoundHeaderView().update({
            heading: this.getFilesFoundHeaderView().getHeading(),  // otherwise it will be erased
            sectionTotalUpdated: null,
            sectionTotalSkipped: null
        });
        this.getFilesFoundBodyView().getStore().removeAll();
        this.getFilesNotFoundBodyView().getStore().removeAll();
        this.getFilesFoundStore().removeAll();
        this.getFilesNotFoundStore().removeAll();
    },

    processNextSearchContainer : function()
    {
        if (this.currentContainerIndex == this.childContainers.length)
        {
            if (!this.haveFilesBeenFound)
            {
                this.getMessagePanel().update('No run data files found in any child folders.');
            }
            this.getRunToolButton().enable();
            this.getFixPathsButton().enable();
        }
        else
        {
            this.fixableFiles = 0;
            this.unfixableFiles = 0;
            this.queryForOrphanedFiles();
        }
    },

    goToNextSearchContainer : function ()
    {
        this.currentContainerIndex++;
        this.updateProgressBar(this.currentContainerIndex, this.containersTotal, true);
        this.processNextSearchContainer();
    },

    queryForOrphanedFiles : function()
    {
        var currentPath = this.childContainers[this.currentContainerIndex].path;
        
        LABKEY.Query.selectRows({
            schemaName: 'exp',
            queryName: 'Data',
            filterArray: [LABKEY.Filter.create('Run', null, LABKEY.Filter.Types.NONBLANK)],
            columns: 'RowId, Name, DataFileUrl, FileExists',
            containerPath: currentPath,
            scope: this,
            success: function(data)
            {
                this.getMessagePanel().show();
                this.checksCompleted = 0;
                this.checksTotal = data.rows.length;

                if (data.rows.length > 0)
                {
                    this.haveFilesBeenFound = true;
                    // call the checkDataFile API for each row
                    Ext4.each(data.rows, function(row)
                    {
                        this.checkIndividualFile(row);
                    }, this);
                }
                else
                {
                    this.goToNextSearchContainer();
                }
            }
        });
    },

    checkIndividualFile : function(row)
    {
        LABKEY.Ajax.request({
            url: LABKEY.ActionURL.buildURL('experiment', 'checkDataFile.api'),
            method: 'POST',
            params: {
                rowId: row['RowId']
            },
            scope: this,
            success: function(response){
                var json = Ext4.decode(response.responseText);
                row['FileExists'] = json.fileExists;
                row['FileExistsAtCurrent'] = json.fileExistsAtCurrent;

                if (row['FileExistsAtCurrent'])
                {
                    this.getFilesFoundStore().add(row);
                    this.fixableFiles++;
                }
                else if (!row['FileExists'])
                {
                    this.getFilesNotFoundStore().add(row);
                    this.unfixableFiles++;
                }
                this.checksCompleted++;
                if(this.checksCompleted === this.checksTotal)
                {
                    var folderData = {};
                    folderData['ContainerPath'] = json.containerPath;
                    folderData['ContainerHref'] = LABKEY.ActionURL.buildURL('project', 'begin', json.containerPath);
                    folderData['UnprocessedFileCount'] = this.checksCompleted;
                    folderData['FixableFileCount'] = this.fixableFiles;
                    folderData['UnfixableFileCount'] = this.unfixableFiles;
                    if(this.unfixableFiles > 0)  // if any unfixable files, mark whole folder
                    {
                        this.getFilesNotFoundFoldersStore().add(folderData);
                    }
                    else if(this.fixableFiles > 0)  // must not be any unfixable files, so add if any fixable files were found
                    {
                        this.getFilesFoundFoldersStore().add(folderData);
                    }
                    this.goToNextSearchContainer();
                }
            }
        });
    },

    updateProgressBar : function(num, denom, asCheck)
    {
        var percentage = num / denom,
                verb = asCheck ? 'Checked run data folders: ' : 'Fixed run data files: ';
        this.getSearchProgressBar().updateProgress(percentage, verb + num + ' of ' + denom);

        this.getSearchProgressBar().show();
        if (num == denom)
        {
            this.getSearchProgressBar().hide();
            this.updateHeaderViews();
            this.getMessagePanel().update('');

            if (asCheck)
            {
                if (this.getFilesFoundStore().getCount() == 0)
                {
                    this.getMessagePanel().update('No run data files to be fixed.');
                    this.getFixPathsButton().hide();
                    this.getConvertStoreToExcelButton().hide();
                }
                else
                {
                    this.getFixPathsButton().show();
                }
            }
        }
    },

    updateHeaderViews : function()
    {
        this.getFilesFoundHeaderView().update({
            heading: this.getFilesFoundHeaderView().getHeading(),  // otherwise it will be erased
            showHeading: (this.getFilesFoundFoldersStore().getCount() != 0)
        });
        this.getFilesFoundHeaderView().setVisible(this.getFilesFoundStore().getCount() > 0);

        this.getFilesNotFoundHeaderView().update({
            heading: this.getFilesNotFoundHeaderView().getHeading(),  // otherwise it will be erased
            showHeading: (this.getFilesNotFoundFoldersStore().getCount() != 0)
        });
        this.getFilesNotFoundHeaderView().setVisible(this.getFilesNotFoundStore().getCount() > 0);
    },

    attemptFixDataFilePaths : function()
    {
        this.getRunToolButton().disable();
        this.getFixPathsButton().disable();
        this.getConvertStoreToExcelButton().disable();
        

        var store = this.getFilesFoundStore();

        this.fixesTotal = store.getCount();
        this.fixesCompleted = 0;
        this.fixesFailed = 0;
        this.totalFixesPerContainer = {};
        this.totalFailsPerContainer = {};
        this.getSearchProgressBar().show();

        Ext4.each(store.getRange(),function(record)  // process all records where data files were found
        {
            LABKEY.Ajax.request({
                url: LABKEY.ActionURL.buildURL('experiment', 'checkDataFile.api'),
                method: 'POST',
                params: {
                    rowId: record.get('RowId'),
                    attemptFilePathFix: true
                },
                scope: this,
                success: function(response){
                    var json = Ext4.decode(response.responseText);
                    if (json.hasOwnProperty('filePathFixed'))
                        record.set('FilePathFixed', json.filePathFixed);
                    if (json.hasOwnProperty('newDataFileUrl'))
                        record.set('NewDataFileUrl', json.newDataFileUrl);

                    this.fixesCompleted++;
                    this.updateProgressBar(this.fixesCompleted + this.fixesFailed, this.fixesTotal, false);

                    // keep track of number of fixes for each container
                    var containerPath = json.containerPath;
                    if (this.totalFixesPerContainer[containerPath] == null)
                        this.totalFixesPerContainer[containerPath] = 1;
                    else
                        this.totalFixesPerContainer[containerPath]++;

                    if (this.fixesCompleted + this.fixesFailed == this.fixesTotal)
                    {
                        this.updateFixTotals();
                        this.cleanUpAfterFixes();
                    }


                },
                failure: function(response){
                    this.fixesFailed++;
                    this.updateProgressBar(this.fixesCompleted + this.fixesFailed, this.fixesTotal, false);

                    // keep track of number of fails for each container
                    var containerPath = json.containerPath;
                    if (this.totalFailsPerContainer[containerPath] == null)
                        this.totalFailsPerContainer[containerPath] = 1;
                    else
                        this.totalFailsPerContainer[containerPath]++;

                    if (this.fixesCompleted + this.fixesFailed == this.fixesTotal)
                    {
                        this.updateFixTotals();
                        this.cleanUpAfterFixes();
                    }
                }
            });
        }, this);

        if(store.getCount() === 0)  // need to clean up, since for-each won't run
        {
            this.cleanUpAfterFixes();
        }
    },

    updateFixTotals : function()
    {
        var filesFoundFoldersStore = this.getFilesFoundFoldersStore();
        var filesNotFoundFoldersStore = this.getFilesNotFoundFoldersStore();

        for (var containerPath in this.totalFixesPerContainer)
        {
            var totalFixes = this.totalFixesPerContainer[containerPath];
            var filesFoundFoldersStoreItem = filesFoundFoldersStore.query('ContainerPath', containerPath, false, true, true);
            var filesNotFoundFoldersStoreItem = filesNotFoundFoldersStore.query('ContainerPath', containerPath, false, true, true);

            if (filesFoundFoldersStoreItem.length == 1)  // should find one and only item for this container path if it exists
            {
                filesFoundFoldersStoreItem.get(0).set('FixedFileCount', totalFixes);
            }
            else if (filesNotFoundFoldersStoreItem.length == 1)  // should find one and only item for this container path if it exists
            {
                filesNotFoundFoldersStoreItem.get(0).set('FixedFileCount', totalFixes);
            }
            else  // should never happen
            {
                this.getMessagePanel().update('Internal error: folder with successful path fix was not found.')
            }
        }
        for (var containerPath in this.totalFailsPerContainer)
        {
            var totalFails = this.totalFailsPerContainer[containerPath];
            var filesFoundFoldersStoreItem = filesFoundFoldersStore.query('ContainerPath', containerPath, false, true, true);
            var filesNotFoundFoldersStoreItem = filesNotFoundFoldersStore.query('ContainerPath', containerPath, false, true, true);
            if (filesFoundFoldersStoreItem.length == 1)  // should find one and only item for this container path if it exists
            {
                filesFoundFoldersStoreItem.get(0).set('FailedFileCount', totalFails);
            }
            else if (filesNotFoundFoldersStoreItem.length == 1)  // should find one and only item for this container path if it exists
            {
                filesNotFoundFoldersStoreItem.get(0).set('FailedFileCount', totalFails);
            }
            else  // should never happen
            {
                this.getMessagePanel().update('Internal error: folder with failed path fix was not found.')
            }
        }
    },

    cleanUpAfterFixes : function()
    {
        this.getRunToolButton().enable();
        this.getFixPathsButton().disable();
        this.getConvertStoreToExcelButton().enable();
        this.getConvertStoreToExcelButton().show();
        this.getFilesFoundHeaderView().update({
            heading: this.getFilesFoundHeaderView().getHeading(),  // otherwise it will be erased
            sectionTotalUpdated: this.fixesCompleted,
            sectionTotalSkipped: this.fixesFailed,
            showHeading: (this.getFilesFoundFoldersStore().getCount() != 0)  // show heading
        });
        this.getFilesNotFoundHeaderView().update({
            heading: this.getFilesNotFoundHeaderView().getHeading(),  // otherwise it will be erased
            showHeading: (this.getFilesNotFoundFoldersStore().getCount() != 0)  // show heading
        });
        this.getFilesFoundBodyView().getStore().sync();
        this.getFilesNotFoundBodyView().getStore().sync();
    },

    convertStoreToExcel : function()
    {
        var store = this.getFilesFoundStore();
        var storeData = [];
        var date = new Date();
        storeData[0] = ['RowId', 'Name', 'DataFileUrl', 'FileExists', 'FileExistsAtCurrent', 'FilePathFixed', 'NewDataFileUrl'];

        Ext4.each(store.getRange(),function(record)  // process all records where data files were fixed
        {
            // convert object to array of values
            var objectValuesAsArray = Object.keys(record.data).map(function (key) {return record.data[key]});
            storeData.push(objectValuesAsArray);
        }, this);

        LABKEY.Utils.convertToExcel(
        {
            fileName : LABKEY.ActionURL.getContainerName() + '_' + Ext4.util.Format.date(date, 'Y-m-d') + '_NAb_Fixes.xls',
            sheets:
            [
                {
                    name: 'Sheet1',
                    data: storeData
                }
            ]
        });
    }
});
