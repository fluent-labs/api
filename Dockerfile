FROM nginx:1.15.2-alpine

# Install node
RUN apk add --update nodejs nodejs-npm

# Build the react app
COPY . /app
WORKDIR /app
RUN npm install && npm run build && mv /app/build /var/www && rm -rf /app

# Set up nginx
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
ENTRYPOINT ["nginx","-g","daemon off;"]
