from django.shortcuts import render
from django.template import loader
from django.http import HttpResponse

# Create your views here.
def submit(request):
    template = loader.get_template('SubmitPage/submit.html')
    context = {}
    return HttpResponse(template.render(context, request))
