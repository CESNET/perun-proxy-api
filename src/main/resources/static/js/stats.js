'use strict';

/* global Chart moment */

function getStatisticsData(name) {
    return $.parseJSON($('#' + name).attr('content'));
}

function getStatisticsDataYMDC(data, field) {
    return data.map(function mapItemToDate(item) {
        return {
            t: new Date(item.date),
            y: item[field]
        };
    });
}

function getTranslation(str) {
    return $.parseJSON($('#translations').attr('content'))[str];
}

function extendData(data, minX, maxX) {
    let i = 0;
    const extendedData = [];
    for (let d = new Date(minX); d <= maxX; d.setDate(d.getDate() + 1)) {
        if (data[i].t.getTime() === d.getTime()) {
            extendedData.push(data[i]);
            i += 1;
        } else if (data[i].t.getTime() > d.getTime()) {
            extendedData.push({ t: new Date(d), y: 0 });
        } else {
            throw new Error("Data is not sorted");
        }
    }
    return extendedData;
}

function drawLoginsChart(data, element) {
    if (!element) return;

    const ctx = element.getContext('2d');

    let usersData = getStatisticsDataYMDC(data, 'users');
    let loginsData = getStatisticsDataYMDC(data, 'logins');

    const minX = Math.min(usersData[0].t, loginsData[0].t);
    const maxX = Math.max(usersData[usersData.length - 1].t, loginsData[loginsData.length - 1].t);

    usersData = extendData(usersData, minX, maxX);
    loginsData = extendData(loginsData, minX, maxX);

    new Chart(ctx, { // eslint-disable-line no-new
        type: 'bar',
        options: {
            responsive: true,
            maintainAspectRatio: false,
            ticks: {
                beginAtZero: true
            },
            scales: {
                xAxes: [{
                    type: 'time',
                    time: { // do not set round: 'day', because it breaks zooming out from 7 or fewer days
                        isoWeekday: true,
                        minUnit: 'day',
                        tooltipFormat: 'l'
                    }
                }],
                yAxes: [{
                    scaleLabel: {
                        display: false
                    }
                }]
            },
            legend: {
                display: false
            },
            tooltips: {
                intersect: false,
                mode: 'index',
                callbacks: {
                    label: function showLabel(tooltipItem, myData) {
                        let label = myData.datasets[tooltipItem.datasetIndex].label || '';
                        if (label) {
                            label += ': ';
                        }
                        label += parseInt(tooltipItem.value, 10);
                        return label;
                    }
                }
            },
            pan: {
                enabled: true,
                mode: 'x',
                rangeMin: {
                    x: minX
                },
                rangeMax: {
                    x: maxX
                }
            },
            zoom: {
                enabled: true,
                drag: false,
                mode: 'x',
                rangeMin: {
                    x: minX
                },
                rangeMax: {
                    x: maxX
                }
            }
        },
        "data": {
            "datasets": [
                {
                    label: getTranslation('of_users'),
                    data: loginsData,
                    type: 'line',
                    pointRadius: 0,
                    fill: false,
                    lineTension: 0,
                    borderWidth: 2,
                    backgroundColor: '#3b3eac',
                    borderColor: '#3b3eac'
                },
                {
                    label: getTranslation('of_logins'),
                    data: usersData,
                    type: 'line',
                    pointRadius: 0,
                    fill: false,
                    lineTension: 0,
                    borderWidth: 2,
                    backgroundColor: '#f90',
                    borderColor: '#f90'
                }
            ]
        }
    });
}

const pieColors = [
    '#3366CC', '#DC3912', '#FF9900', '#109618', '#990099', '#3B3EAC', '#0099C6', '#DD4477', '#66AA00', '#B82E2E',
    '#316395', '#994499', '#22AA99', '#AAAA11', '#6633CC', '#E67300', '#8B0707', '#329262', '#5574A6', '#3B3EAC',
    '#3366CC', '#DC3912', '#FF9900', '#109618', '#990099', '#3B3EAC', '#0099C6', '#DD4477', '#66AA00', '#B82E2E',
    '#316395', '#994499', '#22AA99', '#AAAA11', '#6633CC', '#E67300', '#8B0707', '#329262', '#5574A6', '#3B3EAC'
];

const minPieFraction = 0.005;
const minPieOtherFraction = 0.01;
const maxPieOtherFraction = 0.20;
const pieOtherOnlyIfNeeded = false;

function processDataForPieChart(data, total) {
    if (pieOtherOnlyIfNeeded && data.length <= pieColors.length) {
        return data;
    }
    const dataCols = ['label', 'count', 'link'];
    let i = data.length;
    let othersFraction = 0;
    while (i > 1
           && (i > pieColors.length || (data[i - 1][dataCols[1]] / total < minPieFraction && data[i - 1][dataCols[1]] / total + othersFraction < maxPieOtherFraction)))
    {
        i -= 1;
        othersFraction += data[i][dataCols[1]] / total;
    }
    if (othersFraction < minPieOtherFraction) {
        i = Math.min(data.length, pieColors.length);
        othersFraction = 0;
    }
    const processedData = data.slice(0, i);
    if (i < data.length && othersFraction > 0) {
        const theOthers = [null, null, null];
        theOthers[dataCols[1]] = Math.round(othersFraction * total);
        theOthers[dataCols[0]] = getTranslation('other');
        processedData.push(theOthers);
    }
    return { data: processedData, other: othersFraction > 0, total: total };
}

function drawPieChart(dataset, total, element) {
    if (!element) return;
    const ctx = element.getContext('2d');
    const processedData = processDataForPieChart(dataset, total);
    const data = processedData.data;
    const other = processedData.other;
    const dataCols = ['label', 'count', 'link'];
    const colors = pieColors.slice();
    if (other) {
        colors[data.length - 1] = '#DDDDDD';
    }
    const chart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: data.map(function getFirst(row) {
                return row[dataCols[0]];
            }),
            datasets: [{
                data: data.map(function getSecond(row) {
                    return row[dataCols[1]];
                }),
                backgroundColor: colors,
                borderWidth: 1
            }],
            links: data.map(function getThird(row) {
                return row[dataCols[2]]
            })
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            legend: {
                display: false
            },
            tooltips: {
                callbacks: {
                    label: function generateLabel(tooltipItem, myData) {
                        let label = myData.datasets[tooltipItem.datasetIndex].label || '';
                        if (label) {
                            label += ': ';
                        }
                        const value = myData.datasets[tooltipItem.datasetIndex].data[tooltipItem.index];
                        label += value + ' (';
                        label += Math.round((value / total) * 1000) / 10;
                        label += ' %)';
                        return label;
                    }
                }
            },
            legendCallback: function legendCallback(c) {
                const text = [];
                text.push('<ul class="chart-legend">');

                const datasets = c.data.datasets;
                const labels = c.data.labels;
                const links = c.data.links;

                if (datasets.length) {
                    for (let i = 0; i < datasets[0].data.length; ++i) {
                        const liClasses = ['chart-legend-item'];
                        if (other && i === datasets[0].data.length - 1) {
                            liClasses.push('other');
                        }
                        text.push('<li class="' + liClasses.join(' ') + '">');
                        if (labels[i]) {
                            let label = labels[i];
                            if (links[i] && (!other || i < datasets[0].data.length - 1)) {
                                label = '<a href="' + links[i] + '" class="item">' + label + '</a>';
                            } else {
                                label = '<span class="item">' + label + '</span>';
                            }
                            text.push(label);
                        }
                        text.push('</li>');
                    }
                }

                text.push('</ul>');
                return text.join('');
            }
        }
    });
    if (data) {
        element.addEventListener('click', function pieClick(evt) {
            const activePoints = chart.getElementsAtEvent(evt);
            if (activePoints[0] && data[activePoints[0]._index]['link']) {
                window.location.href = data[activePoints[0]._index]['link'];
            }
        });
    }
    const legendContainer = element.parentNode.parentNode.querySelector('.legend-container');
    legendContainer.innerHTML = chart.generateLegend();
}

function drawCountTable(cols, data, allowHTML, element) {
    if (!element) return;

    const viewCols = ['label', 'count'];

    const tableDiv = element.appendChild(document.createElement('div'));
    tableDiv.className = 'table-responsive';

    const table = tableDiv.appendChild(document.createElement('table'));
    table.className = 'table table-striped table-hover table-condensed';

    const thead = table.appendChild(document.createElement('thead'));
    let tr = thead.appendChild(document.createElement('tr'));
    let th;
    let i;
    for (i = 0; i < viewCols.length; i++) {
        th = tr.appendChild(document.createElement('th'));
        th.innerText = getTranslation(cols[i]);
        if (viewCols[i] === 'count') {
            th.className = 'text-right';
        }
    }

    const tbody = table.appendChild(document.createElement('tbody'));
    let td;
    let a;
    for (let j = 0; j < data.length; j++) {
        tr = tbody.appendChild(document.createElement('tr'));
        for (i = 0; i < viewCols.length; i++) {
            td = tr.appendChild(document.createElement('td'));
            if (viewCols[i] === 'count') {
                td.className = 'text-right';
            }
            if (viewCols[i] === 'label') {
                a = document.createElement('a');
                a[allowHTML ? 'innerHTML' : 'innerText'] = data[j][viewCols[i]];
                a.href = data[j][2];
                td.appendChild(a);
            } else {
                td[allowHTML ? 'innerHTML' : 'innerText'] = data[j][viewCols[i]];
            }
        }
    }

    element.addEventListener('scroll', function floatingTableHead() {
        const scrolling = element.scrollTop > 0;
        element.classList.toggle('scrolling', scrolling);
        element.querySelectorAll('th').forEach(function floatTh(the) {
            the.style.transform = scrolling ? ('translateY(' + element.scrollTop + 'px)') : ''; // eslint-disable-line no-param-reassign
        });
    });
}

function chartInit() {
    // draw overall statistics
    const loginsData = getStatisticsData('logins');
    const idpData = getStatisticsData('loginsIdp');
    const rpData = getStatisticsData('loginsRp');
    const idpTotal = getStatisticsData('loginsIdpTotalCount');
    const rpTotal = getStatisticsData('loginsRpTotalCount');

    drawLoginsChart(loginsData, $('#loginsDashboard')[0]);
    drawPieChart(idpData, idpTotal , $('#summaryIDPChart')[0]);
    drawPieChart(rpData, rpTotal, $('#summaryRPChart')[0]);

    // draw IDP statistics
    drawPieChart(idpData, idpTotal, $('#IDPChart')[0]);
    drawCountTable(['tables_IDP', 'count'], idpData, true, $('#IDPTable')[0]);
    // draw RP statistics
    drawPieChart(rpData, rpTotal, $('#RPChart')[0]);
    drawCountTable(['tables_RP', 'count'], rpData, true, $('#RPTable')[0]);
}

$(document).ready(function docReady() {
    Chart.platform.disableCSSInjection = true;
    const loginsDashboard = document.getElementById('loginsDashboard');
    if (loginsDashboard !== null && loginsDashboard.dataset.locale === 'cs') {
        moment.locale(loginsDashboard.dataset.locale);
    }
    chartInit();
});
