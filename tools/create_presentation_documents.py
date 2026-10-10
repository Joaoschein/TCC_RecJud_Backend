from pathlib import Path
import json
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle
from reportlab.lib.styles import getSampleStyleSheet
from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER
from pypdf import PdfReader
import pypdfium2 as pdfium

root = Path(__file__).resolve().parent
out = root / 'presentation'
out.mkdir(exist_ok=True)
companies = [
    ('Aurora do Vale Indústria Têxtil', 'Porto Alegre', 'EM_ANDAMENTO', '2026-02-12'),
    ('Horizonte Azul Transportes', 'Caxias do Sul', 'AGUARDANDO_APROVACAO', '2026-01-20'),
    ('Jardim das Nuvens Alimentos', 'Santa Maria', 'APROVADO', '2026-01-08'),
    ('Estrela de Âmbar Tecnologia', 'Pelotas', 'EM_ANDAMENTO', '2026-05-18'),
    ('Vale de Cristal Móveis', 'Novo Hamburgo', 'APROVADO', '2026-03-04'),
]
types = ['TRABALHISTA', 'GARANTIA_REAL', 'QUIROGRAFARIO', 'ME_EPP']
names = ['Marina Alves Farias', 'Cooperativa Financeira Sol de Outono', 'Distribuidora Brisa Serena', 'Oficina Ponto de Luz',
         'Rafael Gomes Lacerda', 'Banco Fictício Alameda', 'Suprimentos Campo de Safira', 'Serviços Pequena Aurora',
         'Camila Duarte Nobre', 'Cooperativa Lago de Prata', 'Embalagens Vento do Sul', 'Manutenção Flor de Âmbar',
         'Lucas Ribeiro Valença', 'Financeira Ponte das Nuvens', 'Componentes Nova Constelação', 'Consultoria Jardim Solar',
         'Beatriz Costa Medeiros', 'Cooperativa Rio de Cristal', 'Madeiras Bosque Celeste', 'Oficina Rota Serena']
amounts = [[85000, 1250000, 680000, 42000], [120000, 2400000, 950000, 65000],
           [75000, 800000, 420000, 38000], [98000, 600000, 350000, 27000], [63000, 1100000, 510000, 45000]]
styles = getSampleStyleSheet()
styles['Title'].textColor = colors.HexColor('#17365d')
styles['Title'].fontSize = 22
styles['Title'].leading = 27
styles['BodyText'].leading = 16
styles['BodyText'].spaceAfter = 10
processes, creditors, documents = [], [], []
for i, (company, city, status, opened) in enumerate(companies):
    pid = f'demo-processo-{i+1:02d}'
    process_number = f'900000{i+1}-00.2026.8.21.{100+i:04d}'
    timelines = [dict(id=f'{pid}-evento-1', titulo='Pedido de recuperação apresentado', descricao='Documentação inicial recebida para análise.', data=opened, status='CUMPRIDO'),
                 dict(id=f'{pid}-evento-2', titulo='Relação de credores publicada', descricao='Quadro de credores disponível para conferência.', data='2026-06-10', status='CUMPRIDO'),
                 dict(id=f'{pid}-evento-3', titulo='Assembleia de credores', descricao='Deliberação sobre o plano de recuperação.', data='2026-09-15' if status == 'APROVADO' else '2026-11-20', status='CUMPRIDO' if status == 'APROVADO' else 'PENDENTE')]
    processes.append(dict(id=pid, empresa=company, numeroProcesso=process_number, cnpj=f'00.000.00{i+1}/0001-00', vara='Vara Empresarial - Demonstração', comarca=city, juizResponsavel='Dra. Helena Monteiro (fictícia)', administradorJudicial='Gestão Judicial Caminho Claro (fictícia)', abertura=opened, status=status, timeline=timelines))
    rows = []
    for j, kind in enumerate(types):
        value = amounts[i][j]
        name = names[i*4+j]
        creditors.append(dict(id=f'demo-credor-{i*4+j+1:02d}', processoId=pid, nome=name, documento=f'000.000.{i*4+j+1:03d}-00' if j == 0 else f'00.000.{i*4+j+1:03d}/0001-00', tipo=kind, valor=value, status=['HABILITADO', 'HABILITADO', 'PENDENTE', 'IMPUGNADO'][j]))
        money = f'{value:,.2f}'.replace(',', 'X').replace('.', ',').replace('X', '.')
        rows.append([name, kind.replace('_', ' '), 'R$ ' + money])
    for kind in ['plano', 'relacao']:
        filename = f'{pid}-{kind}.pdf'
        title = 'Plano de recuperação - resumo' if kind == 'plano' else 'Relação de credores - resumo'
        story = [Paragraph('DOCUMENTO FICTÍCIO', styles['Title']), Paragraph(title, styles['Heading2']),
                 Paragraph(company, styles['Heading3']), Paragraph(f'Processo ilustrativo: {process_number}<br/>Comarca: {city}<br/>Abertura: {opened}', styles['BodyText']),
                 Paragraph('Material criado exclusivamente para apresentação acadêmica. Empresas, pessoas, números de processo e documentos de identificação são fictícios e não possuem validade jurídica.', styles['BodyText']), Spacer(1, 12)]
        if kind == 'plano':
            story += [Paragraph('Objetivo da demonstração', styles['Heading2']), Paragraph('Apresentar o acompanhamento de processos, a classificação de credores e a gestão de documentos em um cenário simulado de recuperação judicial.', styles['BodyText']),
                      Paragraph('Etapas do cenário', styles['Heading2']), Paragraph('1. Conferência da documentação inicial.<br/>2. Organização do quadro de credores por classe.<br/>3. Apresentação do plano em assembleia.<br/>4. Acompanhamento dos eventos e indicadores no sistema.', styles['BodyText'])]
        else:
            table_rows = [['Credor fictício', 'Classe', 'Valor']] + [[Paragraph(r[0], styles['BodyText']), Paragraph(r[1], styles['BodyText']), r[2]] for r in rows]
            table = Table(table_rows, colWidths=[210, 150, 115])
            table.setStyle(TableStyle([('BACKGROUND',(0,0),(-1,0),colors.HexColor('#17365d')),('TEXTCOLOR',(0,0),(-1,0),colors.white),('FONTNAME',(0,0),(-1,0),'Helvetica-Bold'),('VALIGN',(0,0),(-1,-1),'TOP'),('GRID',(0,0),(-1,-1),0.4,colors.HexColor('#cbd5e1')),('TOPPADDING',(0,0),(-1,-1),10),('BOTTOMPADDING',(0,0),(-1,-1),10),('ROWBACKGROUNDS',(0,1),(-1,-1),[colors.white,colors.HexColor('#f1f5f9')])]))
            story.append(table)
        def footer(canvas, doc):
            canvas.setFont('Helvetica', 9)
            canvas.setFillColor(colors.HexColor('#64748b'))
            canvas.drawString(48, 35, 'TCC - Dados fictícios para demonstração')
            canvas.drawRightString(547, 35, f'Página {doc.page}')
        SimpleDocTemplate(str(out / filename), pagesize=(595,842), leftMargin=48,rightMargin=48,topMargin=45,bottomMargin=55).build(story, onFirstPage=footer, onLaterPages=footer)
        reader = PdfReader(out / filename)
        assert len(reader.pages) == 1
        assert 'DOCUMENTO FICT' in reader.pages[0].extract_text()
        with pdfium.PdfDocument(str(out / filename)) as pdf:
            pdf[0].render(scale=1.1).to_pil().save(out / filename.replace('.pdf','.png'))
        documents.append(dict(id=f'demo-documento-{len(documents)+1:02d}',processoId=pid,nome=title,nomeArquivo=filename,tipo='Plano de Recuperação' if kind == 'plano' else 'Relação de Credores'))
(out / 'manifest.json').write_text(json.dumps(dict(processos=processes,credores=creditors,documentos=documents),ensure_ascii=False,indent=2),encoding='utf-8')
print('Preparados: 5 empresas/processos, 20 credores e 10 PDFs fictícios; PDFs verificados com uma página cada.')
