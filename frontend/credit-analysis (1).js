/* CreditFlow — Single Customer Credit Analysis Report page logic */

(function () {
    'use strict';

    if (typeof CreditFlowEngine === 'undefined') {
        console.error('CreditFlowEngine is not loaded. Ensure scoring-engine.js is included before credit-analysis.js.');
        return;
    }

    var params = new URLSearchParams(window.location.search);
    var rawId = params.get('id') || 'C00042';
    var customerId = CreditFlowEngine.normalizeId(rawId) || 'C00042';
    if (!CreditFlowEngine.isValidId(customerId)) {
        customerId = 'C00042';
    }

    var report = CreditFlowEngine.buildReport(customerId);
    var f = report.customer;
    var breakdown = report.breakdown;
    var inr = CreditFlowEngine.inr;
    var categories = CreditFlowEngine.CATEGORIES;
    var categoryLabels = CreditFlowEngine.CATEGORY_LABELS;

    function el(id) {
        return document.getElementById(id);
    }

    function set(id, txt) {
        var node = el(id);
        if (node) {
            node.textContent = txt;
        }
    }

    function fillBar(barId, ptsId, lblId, pct, pts, label) {
        var bar = el(barId);
        if (bar) {
            bar.style.width = Math.max(2, Math.min(100, pct)).toFixed(0) + '%';
        }
        set(ptsId, Math.round(pts) + ' pts');
        set(lblId, label);
    }

    function renderReport() {
        set('customerName', f.name);
        set('customerIdBadge', 'ID: ' + f.id);
        document.title = 'Credit Analysis - ' + f.id + ' - CreditFlow';

        set('profIncome', inr(f.income));
        set('profEmi', inr(f.existingEmi));
        var missedEl = el('profMissed');
        missedEl.textContent = f.missedPayments + ' (last 12 months)';
        missedEl.className = 'profile-value ' + (f.missedPayments === 0 ? 'profile-good' : f.missedPayments > 3 ? 'profile-bad' : '');
        set('profBalance', inr(f.currentBalance));
        set('profLimit', inr(f.currentLimit));
        set('profTxnCount', String(f.totalTransactions));

        set('featAvgSpend', inr(f.avgMonthlySpend));
        set('featVolatility', inr(f.spendVolatility));
        set('featMaxTxn', inr(f.maxSingleTransaction));
        set('featSalary', (f.salaryConsistency * 100).toFixed(1) + '%');
        set('featUtil', (f.utilizationRate * 100).toFixed(1) + '%');
        set('featDti', f.debtToIncomeRatio.toFixed(2));
        set('featPayHist', f.paymentHistoryScore.toFixed(2));

        set('baselPD', (report.probabilityOfDefault * 100).toFixed(1) + '%');
        set('baselLGD', (report.lossGivenDefault * 100).toFixed(0) + '%');
        set('baselEL', inr(report.expectedLoss));
        set('baselRWA', inr(report.riskWeightedAssets));

        fillBar('barPayment', 'ptsPayment', 'lblPayment', breakdown.payment.pct, breakdown.payment.pts,
            f.missedPayments === 0 ? 'EXCELLENT - no missed payments' :
                f.missedPayments <= 2 ? 'FAIR - ' + f.missedPayments + ' missed payment(s)' :
                    'POOR - ' + f.missedPayments + ' missed payments');

        fillBar('barUtil', 'ptsUtil', 'lblUtil', breakdown.utilization.pct, breakdown.utilization.pts,
            (f.utilizationRate <= 0.30 ? 'EXCELLENT' : f.utilizationRate <= 0.75 ? 'MODERATE' : 'HIGH') +
            ' - ' + Math.round(f.utilizationRate * 100) + '% used');

        fillBar('barDti', 'ptsDti', 'lblDti', breakdown.dti.pct, breakdown.dti.pts,
            (f.debtToIncomeRatio <= 0.30 ? 'GOOD' : f.debtToIncomeRatio <= 0.55 ? 'MODERATE' : 'HIGH') +
            ' - DTI ' + f.debtToIncomeRatio.toFixed(2));

        fillBar('barStability', 'ptsStability', 'lblStability', breakdown.stability.pct, breakdown.stability.pts,
            f.spendVolatility <= 15000 ? 'STABLE spending' : f.spendVolatility <= 35000 ? 'MODERATE volatility' : 'VOLATILE spending');

        fillBar('barSalary', 'ptsSalary', 'lblSalary', breakdown.salary.pct, breakdown.salary.pts,
            (f.salaryConsistency >= 0.9 ? 'GOOD' : f.salaryConsistency >= 0.6 ? 'FAIR' : 'IRREGULAR') +
            ' - ' + (f.salaryConsistency * 100).toFixed(1) + '% months');

        set('totalScore', report.score + ' / 900');

        var pct = report.percentile;
        set('percentileValue', pct.toFixed(1) + '%');

        var tierLabel = report.decision.approved
            ? (report.decision.tier === 'A' ? 'TIER A (Excellent)' :
                report.decision.tier === 'B' ? 'TIER B (Good)' : 'TIER C (Fair)')
            : 'REJECTED';
        var bandEl = el('scoreBand');
        bandEl.textContent = report.score + ' \u2192 ' + tierLabel;
        bandEl.className = 'band-tier ' + (report.decision.approved ? 'tier-' + report.decision.tier.toLowerCase() : 'tier-rejected');

        var decisionBox = el('decisionBox');
        if (report.decision.approved) {
            var d = report.decision;
            decisionBox.className = 'decision-box approved';
            decisionBox.innerHTML =
                '<div class="decision-badge">\u2713 APPROVED</div>' +
                '<div class="decision-details">' +
                '<div class="decision-row"><span>Credit Tier</span><strong class="tier-' + d.tier.toLowerCase() + '">Tier ' + d.tier + '</strong></div>' +
                '<div class="decision-row"><span>Assigned Credit Limit</span><strong>' + inr(d.assignedLimit) + '</strong></div>' +
                '<div class="decision-row"><span>Interest Rate</span><strong>' + (d.annualRate * 100).toFixed(0) + '% per annum</strong></div>' +
                '<div class="decision-row"><span>Required Capital Reserve</span><strong>' + inr(d.requiredCapital) + '</strong><span class="detail">(Basel III - 8% of limit)</span></div>' +
                '<div class="decision-row"><span>Est. Annual Revenue</span><strong>' + inr(d.expectedRevenue) + '</strong><span class="detail">(bank interest income at 40% APY)</span></div>' +
                '</div>';
        } else {
            decisionBox.className = 'decision-box rejected';
            decisionBox.innerHTML =
                '<div class="decision-badge">\u2715 REJECTED</div>' +
                '<p class="reject-reason">' + report.decision.reason + '</p>' +
                '<div class="improve-tips">' +
                '<div class="tip">Bring missed payments to zero and maintain on-time payments for 6+ months.</div>' +
                '<div class="tip">Reduce credit utilization below 75% by paying down outstanding balances.</div>' +
                '<div class="tip">Lower debt-to-income ratio by reducing EMI obligations or increasing documented income.</div>' +
                '</div>';
        }

        var flags = [];
        if (f.missedPayments === 0) {
            flags.push({ type: 'ok', text: 'No missed payments in the last 12 months.' });
        } else if (f.missedPayments <= 2) {
            flags.push({ type: 'warn', text: f.missedPayments + ' missed payment(s) in the last 12 months.' });
        } else {
            flags.push({ type: 'danger', text: f.missedPayments + ' missed payments - exceeds the 3-payment reject threshold.' });
        }

        if (f.utilizationRate > 0.90) {
            flags.push({ type: 'danger', text: 'Credit utilization at ' + Math.round(f.utilizationRate * 100) + '% - exceeds the 90% reject threshold.' });
        } else if (f.utilizationRate > 0.75) {
            flags.push({ type: 'warn', text: 'Credit utilization at ' + Math.round(f.utilizationRate * 100) + '% - above the 75% penalty threshold.' });
        }

        if (f.debtToIncomeRatio > 0.55) {
            flags.push({ type: 'danger', text: 'Debt-to-income ratio of ' + f.debtToIncomeRatio.toFixed(2) + ' exceeds the 0.55 reject threshold.' });
        } else if (f.debtToIncomeRatio > 0.40) {
            flags.push({ type: 'warn', text: 'Debt-to-income ratio of ' + f.debtToIncomeRatio.toFixed(2) + ' reduces the assignable credit limit.' });
        }

        if (f.salaryConsistency < 0.7) {
            flags.push({ type: 'warn', text: 'Irregular salary credits - only ' + (f.salaryConsistency * 100).toFixed(0) + '% of months show salary income.' });
        }

        if (flags.length === 0 || flags.every(function (flag) { return flag.type === 'ok'; })) {
            flags = [{ type: 'ok', text: 'No significant risk flags detected.' }];
        }

        var flagIcon = { ok: '\u2713', warn: '!', danger: '\u2715' };
        el('riskFlags').innerHTML = flags.map(function (flag) {
            return '<div class="risk-flag ' + flag.type + '"><span class="flag-icon">' + flagIcon[flag.type] + '</span><span>' + flag.text + '</span></div>';
        }).join('');

        var marker = el('percentileMarker');
        if (marker) {
            marker.style.left = pct.toFixed(1) + '%';
        }
        set('percentileCompValue', pct.toFixed(1) + '%');
        set('percentileCompDetail', 'Better than ' + Math.round(pct) + '% of all customers');
        set('compYourScore', String(report.score));
        set('compAvgScore', String(CreditFlowEngine.AVERAGE_SCORE));
        var diff = report.score - CreditFlowEngine.AVERAGE_SCORE;
        var diffEl = el('compScoreDiff');
        diffEl.textContent = (diff >= 0 ? '+' : '') + diff + ' pts';
        diffEl.className = 'score-diff ' + (diff >= 0 ? 'positive' : 'negative');

        set('txnTotal', String(f.totalTransactions));
        set('txnDebits', String(f.debitCount));
        set('txnCredits', String(f.creditCount));
        set('txnTotalDebits', inr(f.totalDebits));
        set('txnTotalCredits', inr(f.totalCredits));

        var recs = [];
        if (f.missedPayments === 0) {
            recs.push({ type: 'positive', icon: '\u2713', text: '<strong>Maintain payment discipline:</strong> Your perfect payment history is your strongest asset. Continue on-time payments to preserve this score.' });
        } else {
            recs.push({ type: 'neutral', icon: '\u2192', text: '<strong>Fix payment history:</strong> ' + f.missedPayments + ' missed payment(s) is the single largest drag on your score. Even one on-time cycle improves this 35%-weighted factor.' });
        }

        if (f.utilizationRate > 0.30) {
            recs.push({ type: 'neutral', icon: '\u2192', text: '<strong>Monitor credit utilization:</strong> Currently at ' + Math.round(f.utilizationRate * 100) + '%. Try to keep it below 30% for optimal score improvement.' });
        } else {
            recs.push({ type: 'positive', icon: '\u2713', text: '<strong>Utilization is well managed:</strong> At ' + Math.round(f.utilizationRate * 100) + '%, you are comfortably under the 30% guideline.' });
        }

        if (f.salaryConsistency >= 0.9) {
            recs.push({ type: 'positive', icon: '\u2713', text: '<strong>Stable income:</strong> Your salary consistency is excellent. Maintain this pattern to qualify for higher tiers in future.' });
        } else {
            recs.push({ type: 'neutral', icon: '\u2192', text: '<strong>Stabilize income:</strong> Salary credits appear in only ' + (f.salaryConsistency * 100).toFixed(0) + '% of months. More regular income documentation improves this factor.' });
        }

        if (f.spendVolatility <= 25000) {
            recs.push({ type: 'positive', icon: '\u2713', text: '<strong>Spending patterns look healthy:</strong> Low volatility and balanced monthly spending demonstrate financial maturity.' });
        } else {
            recs.push({ type: 'neutral', icon: '\u2192', text: '<strong>Smooth out spending:</strong> Week-to-week spend volatility is elevated. More consistent spending improves the stability component of your score.' });
        }

        el('recommendationList').innerHTML = recs.map(function (rec) {
            return '<div class="recommendation-item ' + rec.type + '"><span class="rec-icon">' + rec.icon + '</span><span>' + rec.text + '</span></div>';
        }).join('');

        if (report.decision.approved) {
            var d2 = report.decision;
            el('stepsList').innerHTML =
                '<li>Review your assigned credit limit of <strong>' + inr(d2.assignedLimit) + '</strong></li>' +
                '<li>Interest rate of <strong>' + (d2.annualRate * 100).toFixed(0) + '% p.a.</strong> applies to borrowed amount</li>' +
                '<li>Credit limit will be activated within <strong>3-5 business days</strong></li>' +
                '<li>No annual fees for first year (promotional offer)</li>' +
                '<li>Free credit report access for 12 months</li>';
        } else {
            el('stepsList').innerHTML =
                '<li>Review the rejection reason above and address the underlying factor</li>' +
                '<li>You may reapply after <strong>90 days</strong> once your profile improves</li>' +
                '<li>A free copy of this report is available for your records</li>' +
                '<li>Contact customer support for guidance on credit-building products</li>';
        }
        var nextSteps = el('nextStepsSection');
        if (nextSteps) {
            nextSteps.classList.toggle('rejected-steps', !report.decision.approved);
        }
    }

    function ensureChartsReady() {
        return new Promise(function (resolve) {
            if (typeof Chart === 'undefined') {
                resolve();
                return;
            }

            var scoreCanvas = el('scoreBreakdownChart');
            var categoryCanvas = el('categoryChart');
            var scoreChart = scoreCanvas ? Chart.getChart(scoreCanvas) : null;
            var categoryChartInstance = categoryCanvas ? Chart.getChart(categoryCanvas) : null;

            if (scoreChart) {
                scoreChart.resize();
                scoreChart.update('none');
            }
            if (categoryChartInstance) {
                categoryChartInstance.resize();
                categoryChartInstance.update('none');
            }

            requestAnimationFrame(function () {
                setTimeout(resolve, 300);
            });
        });
    }

    function renderCharts() {
        if (typeof Chart === 'undefined') {
            console.error('Chart.js is not loaded. Charts cannot be rendered.');
            return;
        }

        if (!breakdown || !f.categorySpend) {
            console.error('Report data is incomplete. Missing breakdown or categorySpend.');
            return;
        }

        var categorySpend = f.categorySpend;
        var chartLabels = categories.map(function (cat) {
            return categoryLabels[cat] || cat;
        });
        var chartValues = categories.map(function (cat) {
            return Math.round(categorySpend[cat] || 0);
        });

        withChartLoading('scoreBreakdownChart', function (canvas) {
            new Chart(canvas.getContext('2d'), {
                type: 'radar',
                data: {
                    labels: ['Payment History', 'Credit Utilization', 'Debt-to-Income', 'Spend Stability', 'Salary Consistency'],
                    datasets: [{
                        label: f.id,
                        data: [
                            breakdown.payment.pct,
                            breakdown.utilization.pct,
                            breakdown.dti.pct,
                            breakdown.stability.pct,
                            breakdown.salary.pct
                        ],
                        borderColor: CreditFlowColors.tierB,
                        backgroundColor: 'rgba(37, 99, 235, 0.15)',
                        pointBackgroundColor: CreditFlowColors.tierB,
                        fill: true,
                        pointRadius: 5
                    }, {
                        label: 'Average Customer',
                        data: [65, 60, 65, 70, 75],
                        borderColor: CreditFlowColors.neutral,
                        backgroundColor: 'rgba(148, 163, 184, 0.1)',
                        pointBackgroundColor: CreditFlowColors.neutral,
                        fill: true,
                        pointRadius: 4
                    }]
                },
                options: mergeChartOptions({
                    plugins: {
                        legend: {
                            position: 'bottom'
                        }
                    },
                    scales: {
                        r: {
                            beginAtZero: true,
                            max: 100,
                            ticks: {
                                stepSize: 20,
                                font: { size: 10 }
                            },
                            pointLabels: {
                                font: { size: 11 }
                            }
                        }
                    }
                })
            });
        });

        withChartLoading('categoryChart', function (canvas) {
            new Chart(canvas.getContext('2d'), {
                type: 'bar',
                data: {
                    labels: chartLabels,
                    datasets: [{
                        label: 'Amount Spent (Rs.)',
                        data: chartValues,
                        backgroundColor: CreditFlowColors.chartPalette,
                        borderRadius: 5,
                        borderSkipped: false
                    }]
                },
                options: mergeChartOptions({
                    plugins: {
                        legend: {
                            display: false
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            ticks: {
                                font: { size: 11 }
                            }
                        },
                        x: {
                            ticks: {
                                font: { size: 10 }
                            }
                        }
                    }
                })
            });
        });
    }

    var printTitleRestore = null;

    window.addEventListener('afterprint', function () {
        if (printTitleRestore !== null) {
            document.title = printTitleRestore;
            printTitleRestore = null;
        }
    });

    window.downloadReport = function downloadReport() {
        var notice = el('downloadNotice');
        var cid = f.id;
        var suggestedName = 'CreditReport_' + cid + '.pdf';

        if (notice) {
            notice.textContent = 'Opening print dialog. Choose "Save as PDF" and save as ' + suggestedName + '.';
        }

        ensureChartsReady().then(function () {
            printTitleRestore = document.title;
            document.title = 'CreditReport_' + cid;
            window.print();
        }).catch(function (err) {
            console.error('Print preparation failed:', err);
            if (notice) {
                notice.textContent = 'Could not prepare the report for printing. Please try again.';
            }
        });
    };

    renderReport();
    renderCharts();
})();
